package com.openjar.user_service.service;

import com.openjar.user_service.models.User;
import com.openjar.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Turns a Google identity into an OpenJar account (find-or-create by verified email).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser googleUser = super.loadUser(userRequest);

        String email = googleUser.getEmail();
        if (email == null || !Boolean.TRUE.equals(googleUser.getEmailVerified())) {
            // Never link accounts on an unverified email address.
            throw new OAuth2AuthenticationException(new OAuth2Error("unverified_email",
                    "Your Google email address is not verified", null));
        }
        email = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = User.builder()
                    .userId(UUID.randomUUID().toString())
                    .userName(displayName(googleUser, email))
                    .userEmail(email)
                    // Random, never-disclosed password: Google users sign in through Google only.
                    .password(passwordEncoder.encode(UUID.randomUUID() + UUID.randomUUID().toString()))
                    .isVerified(true)
                    .otpAttempts(0)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);
            log.info("Created account {} via Google sign-in", user.getUserId());
        } else if (!user.isVerifiedAccount()) {
            // Someone registered this email with a password but never proved ownership. Google just
            // did, so take over the pending account and discard the unproven password, preventing a
            // "pre-registration" account takeover.
            user.setIsVerified(true);
            user.setOtp(null);
            user.setOtpExpiresAt(null);
            user.setOtpAttempts(0);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID() + UUID.randomUUID().toString()));
            userRepository.save(user);
            log.info("Verified pending account {} via Google sign-in", user.getUserId());
        }

        return new DefaultOidcUser(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                googleUser.getIdToken(),
                googleUser.getUserInfo(),
                "email");
    }

    private static String displayName(OidcUser googleUser, String email) {
        String name = googleUser.getFullName();
        if (name == null || name.isBlank()) {
            name = email.substring(0, email.indexOf('@'));
        }
        name = name.replaceAll("[^\\p{L}\\p{N} ._-]", "").trim();
        if (name.length() < 3) name = (name + "_chef").trim();
        return name.length() > 50 ? name.substring(0, 50) : name;
    }
}
