package com.openjar.user_service.service;

import com.openjar.user_service.dto.UserRequestDto;
import com.openjar.user_service.dto.UserResponseDto;
import org.springframework.data.domain.Page;

public interface UserService {
    Page<UserResponseDto> getAllUsers(int page, int size);
    UserResponseDto getUserById(String id);

    void createUser(UserRequestDto requestDto);

    void updateUser(String id, UserRequestDto request);

    void deleteUser(String id);
}