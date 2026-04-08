package com.openjar.user_service.service;

import com.openjar.user_service.config.RabbitMQConfig;
import com.openjar.user_service.dto.EmailNotificationDto;
import com.openjar.user_service.dto.UserRequestDto;
import com.openjar.user_service.dto.UserResponseDto;
import com.openjar.user_service.exception.InvalidOtpException;
import com.openjar.user_service.exception.ResourceNotFoundException;
import com.openjar.user_service.exception.UserAlreadyExistsException;
import com.openjar.user_service.models.User;
import com.openjar.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor // Handles all 'final' fields injection automatically
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public Page<UserResponseDto> getAllUsers(int page, int size) {
        log.info("Fetching page {} of users", page);
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAllUsersNative(pageable).map(this::mapToResponseDto);
    }

    @Override
    public UserResponseDto getUserById(String id) {
        User user = userRepository.findUserByIdNative(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponseDto(user);
    }

    @Override
    @Transactional
    public void createUser(UserRequestDto requestDto) {
        if (userRepository.checkEmailExistsNative(requestDto.getUserEmail()) > 0) {
            throw new UserAlreadyExistsException("Account already exists with email: " + requestDto.getUserEmail());
        }

        String newUserId = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        String generatedOtp = generateOTP();

        userRepository.insertUserNative(
                newUserId,
                requestDto.getUserName(),
                requestDto.getUserEmail(),
                encodedPassword,
                generatedOtp
        );

        EmailNotificationDto emailDto = constructWelcomeEmail(requestDto, generatedOtp);

        rabbitTemplate.convertAndSend(
                "openjar_exchange",
                "notification_routing_key",
                emailDto
        );

        log.info("Registration successful. Template event sent for {}", requestDto.getUserEmail());
    }

    @Override
    @Transactional
    public void updateUser(String id, UserRequestDto request) {
        userRepository.findUserByIdNative(id).orElseThrow(() ->
                new ResourceNotFoundException("Update failed. User ID " + id + " not found.")
        );

        userRepository.updateUserNative(id, request.getUserName(), request.getUserEmail());
        log.info("User {} updated.", id);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        userRepository.findUserByIdNative(id).orElseThrow(() ->
                new ResourceNotFoundException("Deletion failed. User ID " + id + " not found.")
        );

        userRepository.deleteUserNative(id);
        log.info("User {} deleted.", id);
    }

    @Override
    @Transactional
    public boolean verifyAccount(String email, String otp) {
        int updatedRows = userRepository.verifyUserNative(email, otp);

        if (updatedRows > 0) {
            log.info("Account verified for {}", email);
            return true;
        } else {
            throw new InvalidOtpException("The OTP provided for " + email + " is incorrect or expired.");
        }
    }

    private String generateOTP() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1000000));
    }

    private EmailNotificationDto constructWelcomeEmail(UserRequestDto requestDto, String otp) {
        Map<String, Object> model = new HashMap<>();
        model.put("userName", requestDto.getUserName());
        model.put("otp", otp);

        return new EmailNotificationDto(
                requestDto.getUserEmail(),
                "Verify Your OpenJar Account",
                "otp-template",
                model
        );
    }

    private UserResponseDto mapToResponseDto(User user) {

        UserResponseDto dto = new UserResponseDto();
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setUserEmail(user.getUserEmail());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}