package com.openjar.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.DelegatingServerAuthenticationEntryPoint;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.DelegatingServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * The gateway is the only thing the browser talks to (Backend-for-Frontend pattern):
 * <ul>
 *   <li>it runs the OAuth2 authorization-code (+PKCE) login against user-service,</li>
 *   <li>keeps the tokens server-side in an HttpOnly session - they never reach JavaScript,</li>
 *   <li>relays the access token to downstream services (TokenRelay), and</li>
 *   <li>protects every state-changing request with a double-submit CSRF token.</li>
 * </ul>
 */
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    private static final String REGISTRATION_ID = "openjar-client";

    @Value("${openjar.frontend-url}")
    private String frontendUrl;

    @Value("${openjar.auth.public-url}")
    private String authPublicUrl;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                            ReactiveClientRegistrationRepository clientRegistrations) {
        DefaultServerOAuth2AuthorizationRequestResolver authorizationRequestResolver =
                new DefaultServerOAuth2AuthorizationRequestResolver(clientRegistrations);
        authorizationRequestResolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());

        RedirectServerAuthenticationSuccessHandler successHandler =
                new RedirectServerAuthenticationSuccessHandler(frontendUrl + "/callback");
        successHandler.setRequestCache(NoOpServerRequestCache.getInstance());

        // XHR calls get a clean 401; browser navigations get sent to the login flow.
        DelegatingServerAuthenticationEntryPoint entryPoint = new DelegatingServerAuthenticationEntryPoint(
                new DelegatingServerAuthenticationEntryPoint.DelegateEntry(
                        ServerWebExchangeMatchers.pathMatchers("/api/**", "/recipe", "/recipe/**"),
                        new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)));
        entryPoint.setDefaultEntryPoint(new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/" + REGISTRATION_ID));

        http
                .authorizeExchange(exchanges -> exchanges
                        // OAuth2 login machinery handled by the gateway itself
                        .pathMatchers("/oauth2/authorization/**", "/login/oauth2/code/**", "/error", "/actuator/health").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/csrf").permitAll()
                        // Public account lifecycle
                        .pathMatchers(HttpMethod.POST, "/api/users/register", "/api/users/verify", "/api/users/resend-otp").permitAll()
                        // Logged-in user only
                        .pathMatchers("/api/users/me").authenticated()
                        // Public read-only browsing
                        .pathMatchers(HttpMethod.GET,
                                "/recipe", "/recipe/**",
                                "/api/users/*",
                                "/api/social/comments/recipe/**",
                                "/api/social/likes/*").permitAll()
                        // API docs (consider disabling in production)
                        .pathMatchers(HttpMethod.GET,
                                "/swagger-ui.html", "/swagger-ui/**", "/webjars/**", "/v3/api-docs/**",
                                "/api/users/v3/api-docs", "/recipe/v3/api-docs", "/api/social/v3/api-docs").permitAll()
                        .anyExchange().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint))
                .requestCache(cache -> cache.requestCache(NoOpServerRequestCache.getInstance()))
                .oauth2Login(login -> login
                        .authorizationRequestResolver(withIdentityProviderHint(authorizationRequestResolver))
                        .authenticationSuccessHandler(successHandler)
                        .authenticationFailureHandler(new RedirectServerAuthenticationFailureHandler(frontendUrl + "/login?error")))
                .oauth2Client(client -> { })
                .logout(logout -> logout
                        .logoutHandler(logoutHandler())
                        .logoutSuccessHandler(oidcLogoutSuccessHandler()))
                .csrf(csrf -> csrf
                        // SPA reads XSRF-TOKEN cookie and echoes it in the X-XSRF-TOKEN header
                        .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new ServerCsrfTokenRequestAttributeHandler()))
                .headers(headers -> headers
                        .referrerPolicy(r -> r.policy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)));

        return http.build();
    }

    /**
     * "/oauth2/authorization/openjar-client?idp=google" forwards an idp=google hint to the
     * authorization server, whose login page then sends the user straight to Google.
     * Only known values are forwarded, so the parameter can't be used to inject anything.
     */
    private static ServerOAuth2AuthorizationRequestResolver withIdentityProviderHint(
            ServerOAuth2AuthorizationRequestResolver delegate) {
        return new ServerOAuth2AuthorizationRequestResolver() {
            @Override
            public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange) {
                return delegate.resolve(exchange).map(request -> addHint(exchange, request));
            }

            @Override
            public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange, String clientRegistrationId) {
                return delegate.resolve(exchange, clientRegistrationId).map(request -> addHint(exchange, request));
            }
        };
    }

    /** Hints the SPA may pass through to the login page, with their only accepted values. */
    private static final java.util.Map<String, java.util.Set<String>> ALLOWED_HINTS = java.util.Map.of(
            "idp", java.util.Set.of("google"),           // skip the login page, go straight to Google
            "theme", java.util.Set.of("light", "dark")); // render the login page in the app's theme

    private static OAuth2AuthorizationRequest addHint(ServerWebExchange exchange, OAuth2AuthorizationRequest request) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(request.getAuthorizationRequestUri());
        boolean changed = false;
        for (var hint : ALLOWED_HINTS.entrySet()) {
            String value = exchange.getRequest().getQueryParams().getFirst(hint.getKey());
            if (value != null && hint.getValue().contains(value)) {
                uri.queryParam(hint.getKey(), value);
                changed = true;
            }
        }
        return changed
                ? OAuth2AuthorizationRequest.from(request).authorizationRequestUri(uri.build(true).toUriString()).build()
                : request;
    }

    /** Clears the security context AND the whole web session (which holds the OAuth2 tokens). */
    private ServerLogoutHandler logoutHandler() {
        ServerLogoutHandler invalidateSession = (exchange, authentication) ->
                exchange.getExchange().getSession().flatMap(WebSession::invalidate);
        return new DelegatingServerLogoutHandler(new SecurityContextServerLogoutHandler(), invalidateSession);
    }

    /** RP-initiated logout: also ends the session at the authorization server, then returns to the SPA. */
    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler() {
        DefaultServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();
        return (exchange, authentication) -> {
            String target = frontendUrl + "/";
            String location = target;
            if (authentication != null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
                location = UriComponentsBuilder.fromUriString(authPublicUrl + "/connect/logout")
                        .queryParam("id_token_hint", oidcUser.getIdToken().getTokenValue())
                        .queryParam("post_logout_redirect_uri", target)
                        .encode()
                        .build()
                        .toUriString();
            }
            return redirectStrategy.sendRedirect(exchange.getExchange(), URI.create(location));
        };
    }

    /**
     * Reactive CSRF tokens are lazy; subscribing here makes sure the XSRF-TOKEN cookie
     * is written on every response so the SPA always has a token to echo back.
     */
    @Bean
    public WebFilter csrfCookieWebFilter() {
        return (exchange, chain) -> {
            Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
            return (csrfToken != null ? csrfToken : Mono.<CsrfToken>empty())
                    .then(chain.filter(exchange));
        };
    }

    /** Cheap endpoint the SPA can hit to obtain a CSRF cookie before its first POST. */
    @Bean
    public RouterFunction<ServerResponse> csrfRoute() {
        return RouterFunctions.route()
                .GET("/api/csrf", request -> ServerResponse.noContent().build())
                .build();
    }
}
