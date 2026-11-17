package com.openjar.user_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;

/** Renders the branded sign-in page of the authorization server. */
@Controller
public class LoginController {

    private final boolean googleEnabled;
    private final String frontendUrl;
    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

    public LoginController(ObjectProvider<ClientRegistrationRepository> googleRegistrations,
                           @Value("${openjar.frontend-url}") String frontendUrl) {
        this.googleEnabled = googleRegistrations.getIfAvailable() != null;
        this.frontendUrl = frontendUrl;
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        HttpServletRequest request, HttpServletResponse response, Model model,
                        CsrfToken csrfToken) {
        SavedRequest saved = requestCache.getRequest(request, response);

        // The SPA's "Continue with Google" button sends idp=google through the OAuth2 request:
        // skip this page and go straight to Google (only on a clean first visit, to avoid loops).
        if (googleEnabled && error == null && saved != null
                && Arrays.asList(nullSafe(saved.getParameterValues("idp"))).contains("google")) {
            return "redirect:/oauth2/authorization/google";
        }

        // Resolve the (lazy, session-backed) CSRF token now, before the template starts streaming.
        model.addAttribute("csrfParam", csrfToken.getParameterName());
        model.addAttribute("csrfValue", csrfToken.getToken());
        // Theme chosen in the SPA (forwarded through the OAuth2 request); only two values are accepted.
        String theme = saved == null ? null : firstOf(saved.getParameterValues("theme"));
        model.addAttribute("theme", "dark".equals(theme) || "light".equals(theme) ? theme : null);
        model.addAttribute("googleEnabled", googleEnabled);
        model.addAttribute("frontendUrl", frontendUrl);
        model.addAttribute("fromApp", saved != null);
        model.addAttribute("errorKind", errorKind(error, request.getSession(false)));
        return "login";
    }

    private static String errorKind(String error, HttpSession session) {
        if (error == null) return null;
        if ("google".equals(error)) return "google";
        Object last = session == null ? null : session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        return last instanceof DisabledException ? "unverified" : "credentials";
    }

    private static String firstOf(String[] values) {
        return values == null || values.length == 0 ? null : values[0];
    }

    private static String[] nullSafe(String[] values) {
        return values == null ? new String[0] : values;
    }
}
