package com.openjar.user_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

/**
 * "Sign in with Google" is federated here, at the authorization server, so every downstream
 * service still receives the same OpenJar-issued tokens no matter how the user signed in.
 *
 * Enabled only when GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET are set. In Google Cloud Console
 * register the redirect URI:  {AUTH_ISSUER}/login/oauth2/code/google
 * (locally: http://localhost:8086/login/oauth2/code/google)
 */
@Configuration
@ConditionalOnExpression("'${openjar.google.client-id:}' != '' and '${openjar.google.client-secret:}' != ''")
public class GoogleLoginConfig {

    public static final String REGISTRATION_ID = "google";

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${openjar.google.client-id}") String clientId,
            @Value("${openjar.google.client-secret}") String clientSecret) {
        return new InMemoryClientRegistrationRepository(
                CommonOAuth2Provider.GOOGLE.getBuilder(REGISTRATION_ID)
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .scope("openid", "profile", "email")
                        // Our users are keyed by email, so make the principal name the email.
                        .userNameAttributeName("email")
                        .build());
    }
}
