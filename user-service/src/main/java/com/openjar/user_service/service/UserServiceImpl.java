package com.openjar.user_service.service;

import com.openjar.user_service.dto.EmailNotificationDto;
import com.openjar.user_service.dto.PublicUserDto;
import com.openjar.user_service.dto.UserRequestDto;
import com.openjar.user_service.dto.UserResponseDto;
import com.openjar.user_service.dto.UserUpdateDto;
import com.openjar.user_service.exception.InvalidOtpException;
import com.openjar.user_service.exception.ResourceNotFoundException;
import com.openjar.user_service.exception.TooManyRequestsException;
import com.openjar.user_service.exception.UserAlreadyExistsException;
import com.openjar.user_service.models.User;
import com.openjar.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    static final Duration OTP_TTL = Duration.ofMinutes(10);
    static final Duration OTP_RESEND_COOLDOWN = Duration.ofSeconds(60);
    static final int OTP_MAX_ATTEMPTS = 5;
    private static final int MAX_PAGE_SIZE = 50;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;

    @Value("${openjar.dev.log-otp:false}")
    private boolean devLogOtp;

    @Override
    public Page<PublicUserDto> getAllUsers(int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return userRepository.findAll(PageRequest.of(Math.max(page, 0), safeSize, Sort.by("createdAt").descending()))
                .map(this::toPublicDto);
    }

    @Override
    public PublicUserDto getPublicUser(String id) {
        return toPublicDto(findById(id));
    }

    @Override
    public UserResponseDto getUserById(String id) {
        return toResponseDto(findById(id));
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .map(this::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public void createUser(UserRequestDto requestDto) {
        String email = normalizeEmail(requestDto.getUserEmail());
        var existing = userRepository.findByEmail(email);

        if (existing.isPresent()) {
            User user = existing.get();
            if (user.isVerifiedAccount()) {
                throw new UserAlreadyExistsException("An account with this email already exists");
            }
            // Unverified account: just issue a fresh code. The stored password is intentionally
            // NOT overwritten, so nobody can hijack a pending registration.
            issueOtp(user);
            return;
        }

        User user = User.builder()
                .userId(UUID.randomUUID().toString())
                .userName(requestDto.getUserName().trim())
                .userEmail(email)
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .isVerified(false)
                .createdAt(LocalDateTime.now())
                .build();
        issueOtp(user);
        log.info("Registered new user {}", user.getUserId());
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(String id, UserUpdateDto request) {
        User user = findById(id);
        user.setUserName(request.getUserName().trim());
        userRepository.save(user);
        log.info("User {} updated their profile", id);
        return toResponseDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        User user = findById(id);
        userRepository.delete(user);
        log.info("User {} deleted their account", id);
    }

    @Override
    @Transactional(noRollbackFor = InvalidOtpException.class)
    public void verifyAccount(String email, String otp) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .filter(u -> !u.isVerifiedAccount() && u.getOtp() != null)
                .orElseThrow(() -> new InvalidOtpException("Invalid email or code"));

        int attempts = user.getOtpAttempts() == null ? 0 : user.getOtpAttempts();
        if (attempts >= OTP_MAX_ATTEMPTS) {
            throw new InvalidOtpException("Too many failed attempts. Request a new code.");
        }
        if (user.getOtpExpiresAt() == null || LocalDateTime.now().isAfter(user.getOtpExpiresAt())) {
            throw new InvalidOtpException("This code has expired. Request a new code.");
        }
        if (!passwordEncoder.matches(otp, user.getOtp())) {
            user.setOtpAttempts(attempts + 1);
            userRepository.save(user);   // persisted despite the exception (noRollbackFor)
            throw new InvalidOtpException("Invalid email or code");
        }

        user.setIsVerified(true);
        user.setOtp(null);
        user.setOtpExpiresAt(null);
        user.setOtpAttempts(0);
        userRepository.save(user);
        log.info("Account verified for user {}", user.getUserId());
    }

    @Override
    @Transactional
    public void resendOtp(String email) {
        // Always behaves the same from the caller's point of view, to avoid account enumeration.
        userRepository.findByEmail(normalizeEmail(email))
                .filter(u -> !u.isVerifiedAccount())
                .ifPresent(this::issueOtp);
    }

    // ---------------------------------------------------------------------------------------

    private void issueOtp(User user) {
        LocalDateTime now = LocalDateTime.now();
        if (user.getOtpExpiresAt() != null
                && user.getOtpExpiresAt().minus(OTP_TTL).plus(OTP_RESEND_COOLDOWN).isAfter(now)) {
            throw new TooManyRequestsException("Please wait a minute before requesting another code");
        }

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        user.setOtp(passwordEncoder.encode(otp));
        user.setOtpExpiresAt(now.plus(OTP_TTL));
        user.setOtpAttempts(0);
        userRepository.save(user);

        if (devLogOtp) {
            // Local development only (DEV_LOG_OTP=true). Never enable this in a shared environment.
            log.warn("[DEV_LOG_OTP] Verification code for {}: {}", user.getUserEmail(), otp);
        }

        Map<String, Object> model = new HashMap<>();
        model.put("userName", user.getUserName());
        model.put("otp", otp);
        rabbitTemplate.convertAndSend("openjar_exchange", "notification_routing_key",
                new EmailNotificationDto(user.getUserEmail(), "Verify Your OpenJar Account", "otp-template", model));
    }

    private User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private PublicUserDto toPublicDto(User user) {
        return new PublicUserDto(user.getUserId(), user.getUserName(), user.getCreatedAt());
    }

    private UserResponseDto toResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setUserEmail(user.getUserEmail());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
