package com.openjar.user_service.controller;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Lets the SPA know which sign-in methods to offer (e.g. hide Google when it isn't configured). */
@RestController
public class AuthOptionsController {

    private final boolean googleEnabled;

    public AuthOptionsController(ObjectProvider<ClientRegistrationRepository> googleRegistrations) {
        this.googleEnabled = googleRegistrations.getIfAvailable() != null;
    }

    @GetMapping("/api/users/auth-options")
    public Map<String, Boolean> authOptions() {
        return Map.of("google", googleEnabled);
    }
}
