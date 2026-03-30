package com.openjar.user_service.service;

import com.openjar.user_service.dto.UserRequestDto;
import com.openjar.user_service.dto.UserResponseDto;
import org.springframework.data.domain.Page;

public interface UserService {
    Page<UserResponseDto> getAllUsers(int page, int size);
    UserResponseDto getUserById(Long id);
    void createUser(UserRequestDto requestDto);
    void updateUser(Long id, UserRequestDto requestDto);
    void deleteUser(Long id);
}