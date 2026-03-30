package com.openjar.user_service.service;


import com.openjar.user_service.dto.UserRequestDto;
import com.openjar.user_service.dto.UserResponseDto;

import java.util.List;

public interface UserService {
    List<UserResponseDto> getAllUsers();
    UserResponseDto getUserById(Long id);
    void createUser(UserRequestDto requestDto);
    void updateUser(Long id, UserRequestDto requestDto);
    void deleteUser(Long id);
}