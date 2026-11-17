package com.openjar.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Logs one line per request with its total time, and stamps every request with an
 * X-Request-Id that is forwarded to the services (and returned to the browser) so a
 * single user action can be followed through all logs.
 *
 * Only the path is logged, never the query string (it can carry OAuth codes / state).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTimingWebFilter implements WebFilter {

    public static final String REQUEST_ID = "X-Request-Id";
    private static final Logger log = LoggerFactory.getLogger("openjar.timing.http");
    /** Accept a caller-supplied id only if it is short and harmless (prevents log injection). */
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9-]{6,64}");

    private final long slowMs;

    public RequestTimingWebFilter(@Value("${openjar.timing.slow-ms:1000}") long slowMs) {
        this.slowMs = slowMs;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long start = System.nanoTime();
        String incoming = exchange.getRequest().getHeaders().getFirst(REQUEST_ID);
        String id = incoming != null && SAFE_ID.matcher(incoming).matches()
                ? incoming
                : UUID.randomUUID().toString().substring(0, 8);

        ServerHttpRequest request = exchange.getRequest().mutate().headers(h -> h.set(REQUEST_ID, id)).build();
        ServerWebExchange tracked = exchange.mutate().request(request).build();
        tracked.getResponse().getHeaders().set(REQUEST_ID, id);

        return chain.filter(tracked).doFinally(signal -> {
            long ms = (System.nanoTime() - start) / 1_000_000;
            String path = request.getURI().getRawPath();
            HttpStatusCode status = tracked.getResponse().getStatusCode();
            Route route = tracked.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            String target = route != null ? route.getId() : "gateway";
            String outcome = signal == SignalType.CANCEL ? "CANCELLED" : String.valueOf(status == null ? 200 : status.value());

            String line = String.format("[%s] %s %s -> %s in %d ms (via %s)",
                    id, request.getMethod(), path, outcome, ms, target);
            if (path.startsWith("/actuator")) {
                log.debug(line);
            } else if (ms >= slowMs) {
                log.warn("{}  SLOW", line);
            } else {
                log.info(line);
            }
        });
    }
}
