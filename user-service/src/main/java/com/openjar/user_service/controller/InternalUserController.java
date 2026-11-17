package com.openjar.user_service.controller;

import com.openjar.user_service.dto.UserResponseDto;
import com.openjar.user_service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Service-to-service lookup that includes the email address (needed for notifications).
 * Not routed by the API gateway and guarded by a shared secret (INTERNAL_API_TOKEN).
 */
@Slf4j
@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    public static final String HEADER = "X-Internal-Token";

    private final UserService userService;
    private final byte[] expectedToken;

    public InternalUserController(UserService userService,
                                  @Value("${openjar.internal-token:}") String internalToken) {
        this.userService = userService;
        this.expectedToken = internalToken.getBytes(StandardCharsets.UTF_8);
        if (!StringUtils.hasText(internalToken)) {
            log.warn("INTERNAL_API_TOKEN is not set: internal user lookups (email notifications) are disabled.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable String id,
                                                   @RequestHeader(value = HEADER, required = false) String token) {
        if (expectedToken.length == 0 || token == null
                || !MessageDigest.isEqual(expectedToken, token.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
