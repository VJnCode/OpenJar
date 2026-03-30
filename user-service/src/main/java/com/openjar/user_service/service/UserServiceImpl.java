package com.openjar.user_service.service;

import com.openjar.user_service.dto.UserRequestDto;
import com.openjar.user_service.dto.UserResponseDto;
import com.openjar.user_service.exception.ResourceNotFoundException;
import com.openjar.user_service.exception.UserAlreadyExistsException;
import com.openjar.user_service.models.User;
import com.openjar.user_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<UserResponseDto> getAllUsers() {
        log.info("Fetching all users from the database");
        return userRepository.findAllUsersNative()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        User user = userRepository.findUserByIdNative(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });
        return mapToResponseDto(user);
    }

    @Override
    public void createUser(UserRequestDto request) {
        log.info("Attempting to create new user with email: {}", request.getUserEmail());

        if (userRepository.countByUserEmailNative(request.getUserEmail()) > 0) {
            log.warn("Creation failed. Email already exists: {}", request.getUserEmail());
            throw new UserAlreadyExistsException("Email is already registered!");
        }

        if (userRepository.countByUserNameNative(request.getUserName()) > 0) {
            log.warn("Creation failed. Username already taken: {}", request.getUserName());
            throw new UserAlreadyExistsException("Username is already taken!");
        }

        userRepository.insertUserNative(request.getUserName(), request.getUserEmail(), request.getPassword());
        log.info("User created successfully with email: {}", request.getUserEmail());
    }

    @Override
    public void updateUser(Long id, UserRequestDto request) {
        log.info("Attempting to update user with ID: {}", id);

        userRepository.findUserByIdNative(id).orElseThrow(() -> {
            log.error("Update failed. User not found with ID: {}", id);
            return new ResourceNotFoundException("User not found with id: " + id);
        });

        userRepository.updateUserNative(id, request.getUserName(), request.getUserEmail());
        log.info("User updated successfully with ID: {}", id);
    }

    @Override
    public void deleteUser(Long id) {
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
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}