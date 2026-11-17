package com.openjar.socialservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Stateless resource server. Reads are public; every write needs a JWT issued by user-service,
 * and the acting user is always taken from that token (never from the URL, body or a header).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,
                                "/api/social/comments/recipe/**",
                                "/api/social/likes/*",
                                "/api/social/v3/api-docs", "/api/social/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health", "/error").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
