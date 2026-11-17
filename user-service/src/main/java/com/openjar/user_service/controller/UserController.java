package com.openjar.user_service.controller;

import com.openjar.user_service.dto.PublicUserDto;
import com.openjar.user_service.dto.ResendOtpRequestDto;
import com.openjar.user_service.dto.UserRequestDto;
import com.openjar.user_service.dto.UserResponseDto;
import com.openjar.user_service.dto.UserUpdateDto;
import com.openjar.user_service.dto.VerifyRequestDto;
import com.openjar.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Public directory: no email addresses. Requires login. */
    @GetMapping
    public ResponseEntity<Page<PublicUserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    /** The currently logged-in user (includes email). */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> me(@AuthenticationPrincipal Jwt jwt) {
        String uid = jwt.getClaimAsString("uid");
        return ResponseEntity.ok(uid != null ? userService.getUserById(uid) : userService.getUserByEmail(jwt.getSubject()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateMe(@AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody UserUpdateDto requestDto) {
        return ResponseEntity.ok(userService.updateUser(currentUserId(jwt), requestDto));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteMe(@AuthenticationPrincipal Jwt jwt) {
        userService.deleteUser(currentUserId(jwt));
        return ResponseEntity.ok(Map.of("message", "Account deleted"));
    }

    /** Public profile of any user: no email address. */
    @GetMapping("/{id}")
    public ResponseEntity<PublicUserDto> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getPublicUser(id));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> createUser(@Valid @RequestBody UserRequestDto requestDto) {
        userService.createUser(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Check your inbox for a 6-digit verification code."));
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyUser(@Valid @RequestBody VerifyRequestDto requestDto) {
        userService.verifyAccount(requestDto.getEmail(), requestDto.getOtp());
        return ResponseEntity.ok(Map.of("message", "Your account is verified. You can sign in now."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(@Valid @RequestBody ResendOtpRequestDto requestDto) {
        userService.resendOtp(requestDto.getEmail());
        return ResponseEntity.ok(Map.of("message", "If that account is awaiting verification, a new code is on its way."));
    }

    private String currentUserId(Jwt jwt) {
        String uid = jwt.getClaimAsString("uid");
        return uid != null ? uid : userService.getUserByEmail(jwt.getSubject()).getUserId();
    }
}
