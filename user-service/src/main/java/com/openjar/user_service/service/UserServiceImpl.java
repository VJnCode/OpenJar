package com.openjar.user_service.service;

import com.openjar.user_service.config.RabbitMQConfig;
import com.openjar.user_service.dto.EmailNotificationDto;
import com.openjar.user_service.dto.UserRequestDto;
import com.openjar.user_service.dto.UserResponseDto;
import com.openjar.user_service.exception.ResourceNotFoundException;
import com.openjar.user_service.exception.UserAlreadyExistsException;
import com.openjar.user_service.models.User;
import com.openjar.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final RabbitTemplate rabbitTemplate;

    @Override
    public Page<UserResponseDto> getAllUsers(int page, int size) {
        log.info("Fetching page {} of users with size {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAllUsersNative(pageable)
                .map(this::mapToResponseDto);
    }


    @Override
    public UserResponseDto getUserById(String id) {
        log.info("Fetching user with ID: {}", id);
        User user = userRepository.findUserByIdNative(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });
        return mapToResponseDto(user);
    }

    private String generateOTP() {
        SecureRandom random = new SecureRandom();
        int otp = random.nextInt(1000000);
        return String.format("%06d", otp); // Ensures it's always 6 digits, e.g., "004021"
    }

    @Override
    public void createUser(UserRequestDto requestDto) {
        if (userRepository.checkEmailExistsNative(requestDto.getUserEmail()) > 0) {
            throw new IllegalArgumentException("Email already exists.");
        }

        String newUserId = java.util.UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        String generatedOtp = generateOTP();

        userRepository.insertUserNative(
                newUserId,
                requestDto.getUserName(),
                requestDto.getUserEmail(),
                encodedPassword,
                generatedOtp
        );

        EmailNotificationDto emailDto = new EmailNotificationDto(
                requestDto.getUserEmail(),
                generatedOtp,
                "Welcome to OpenJar - Verify Your Account"
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, emailDto);

        log.info("OTP event published to RabbitMQ for {}", requestDto.getUserEmail());
    }

    @Override
    public boolean verifyAccount(String email, String otp) {
        int updatedRows = userRepository.verifyUserNative(email, otp);

        if (updatedRows > 0) {
            log.info("✅ SUCCESS: Account successfully verified for {}", email);
            return true;
        } else {
            log.warn("❌ VERIFICATION FAILED: Invalid OTP attempt for {}", email);
            return false;
        }
    }


    @Override
    public void updateUser(String id, UserRequestDto request) {
        log.info("Attempting to update user with ID: {}", id);

        userRepository.findUserByIdNative(id).orElseThrow(() -> {
            log.error("Update failed. User not found with ID: {}", id);
            return new ResourceNotFoundException("User not found with id: " + id);
        });

        userRepository.updateUserNative(id, request.getUserName(), request.getUserEmail());
        log.info("User updated successfully with ID: {}", id);
    }

    @Override
    public void deleteUser(String id) {
        log.info("Attempting to delete user with ID: {}", id);

        userRepository.findUserByIdNative(id).orElseThrow(() -> {
            log.error("Deletion failed. User not found with ID: {}", id);
            return new ResourceNotFoundException("User not found with id: " + id);
        });

        userRepository.deleteUserNative(id);
        log.info("User deleted successfully with ID: {}", id);
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