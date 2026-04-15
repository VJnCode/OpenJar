package com.openjar.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/login/**",
                                "/logout",
                                "/oauth2/**",
                                "/.well-known/**",
                                "/userinfo",
                                "/api/users/register",
                                "/api/users/verify",
                                "/error",

                                // --- SWAGGER UI CORE FILES ---
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/v3/api-docs/**",

                                // --- MICROSERVICE DOC PATHS (THE FIX!) ---
                                "/api/users/v3/api-docs",
                                "/recipe/v3/api-docs",
                                "/api/social/v3/api-docs",
                                "/api/notifications/v3/api-docs"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}