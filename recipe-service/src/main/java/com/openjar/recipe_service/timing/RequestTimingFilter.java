package com.openjar.recipe_service.timing;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * One log line per HTTP request with status and duration. The X-Request-Id set by the API
 * gateway is put into the logging MDC, so every log line produced while handling the request
 * (including operation / DB timings) carries the same id. Query strings are never logged.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTimingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID = "X-Request-Id";
    public static final String MDC_KEY = "requestId";
    private static final Logger log = LoggerFactory.getLogger("openjar.timing.http");
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9-]{6,64}");

    private final long slowMs;

    public RequestTimingFilter(@Value("${openjar.timing.slow-ms:1000}") long slowMs) {
        this.slowMs = slowMs;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long start = System.nanoTime();
        String incoming = request.getHeader(REQUEST_ID);
        String id = incoming != null && SAFE_ID.matcher(incoming).matches()
                ? incoming : UUID.randomUUID().toString().substring(0, 8);
        MDC.put(MDC_KEY, id);
        response.setHeader(REQUEST_ID, id);
        try {
            chain.doFilter(request, response);
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000;
            String path = request.getRequestURI();
            String line = String.format("%s %s -> %d in %d ms", request.getMethod(), path, response.getStatus(), ms);
            if (path.startsWith("/actuator")) log.debug(line);
            else if (ms >= slowMs) log.warn("{}  SLOW", line);
            else log.info(line);
            MDC.remove(MDC_KEY);
        }
    }
}
