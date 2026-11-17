package com.openjar.user_service;

import com.openjar.user_service.dto.UserRequestDto;
import com.openjar.user_service.exception.InvalidOtpException;
import com.openjar.user_service.exception.UserAlreadyExistsException;
import com.openjar.user_service.models.User;
import com.openjar.user_service.repository.UserRepository;
import com.openjar.user_service.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder, rabbitTemplate);
    }

    private static UserRequestDto request(String email) {
        UserRequestDto request = new UserRequestDto();
        request.setUserName("varun_test");
        request.setUserEmail(email);
        request.setPassword("plainTextPassword");
        return request;
    }

    @Test
    void createUser_hashesPasswordAndOtp_andNormalizesEmail() {
        when(userRepository.findByEmail("varun@example.com")).thenReturn(Optional.empty());

        userService.createUser(request("  Varun@Example.com "));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        User user = saved.getValue();
        assertEquals("varun@example.com", user.getUserEmail());
        assertTrue(passwordEncoder.matches("plainTextPassword", user.getPassword()));
        assertFalse(user.isVerifiedAccount());
        assertNotNull(user.getOtp());
        assertFalse(user.getOtp().matches("\\d{6}"), "OTP must not be stored in plain text");
        assertTrue(user.getOtpExpiresAt().isAfter(LocalDateTime.now()));
        verify(rabbitTemplate).convertAndSend(eq("openjar_exchange"), eq("notification_routing_key"), any(Object.class));
    }

    @Test
    void createUser_fails_whenVerifiedAccountExists() {
        User existing = User.builder().userEmail("dup@example.com").isVerified(true).build();
        when(userRepository.findByEmail("dup@example.com")).thenReturn(Optional.of(existing));

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(request("dup@example.com")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_forPendingAccount_doesNotOverwritePassword() {
        User pending = User.builder().userEmail("p@example.com").password("original-hash").isVerified(false).build();
        when(userRepository.findByEmail("p@example.com")).thenReturn(Optional.of(pending));

        userService.createUser(request("p@example.com"));

        assertEquals("original-hash", pending.getPassword());
        assertNotNull(pending.getOtp());
    }

    @Test
    void verify_wrongCode_incrementsAttempts() {
        User pending = pendingUserWithOtp("123456", 0);
        when(userRepository.findByEmail("p@example.com")).thenReturn(Optional.of(pending));

        assertThrows(InvalidOtpException.class, () -> userService.verifyAccount("p@example.com", "000000"));
        assertEquals(1, pending.getOtpAttempts());
        assertFalse(pending.isVerifiedAccount());
    }

    @Test
    void verify_isLockedAfterMaxAttempts_evenWithCorrectCode() {
        User pending = pendingUserWithOtp("123456", 5);
        when(userRepository.findByEmail("p@example.com")).thenReturn(Optional.of(pending));

        assertThrows(InvalidOtpException.class, () -> userService.verifyAccount("p@example.com", "123456"));
        assertFalse(pending.isVerifiedAccount());
    }

    @Test
    void verify_expiredCode_isRejected() {
        User pending = pendingUserWithOtp("123456", 0);
        pending.setOtpExpiresAt(LocalDateTime.now().minusSeconds(1));
        when(userRepository.findByEmail("p@example.com")).thenReturn(Optional.of(pending));

        assertThrows(InvalidOtpException.class, () -> userService.verifyAccount("p@example.com", "123456"));
    }

    @Test
    void verify_correctCode_activatesAccount() {
        User pending = pendingUserWithOtp("123456", 2);
        when(userRepository.findByEmail("p@example.com")).thenReturn(Optional.of(pending));

        userService.verifyAccount("p@example.com", "123456");

        assertTrue(pending.isVerifiedAccount());
        assertNull(pending.getOtp());
    }

    private User pendingUserWithOtp(String otp, int attempts) {
        return User.builder()
                .userEmail("p@example.com")
                .isVerified(false)
                .otp(passwordEncoder.encode(otp))
                .otpAttempts(attempts)
                .otpExpiresAt(LocalDateTime.now().plusMinutes(5))
                .build();
    }
}
