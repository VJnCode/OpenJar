package com.openjar.user_service.service;

import com.openjar.user_service.dto.PublicUserDto;
import com.openjar.user_service.dto.UserRequestDto;
import com.openjar.user_service.dto.UserResponseDto;
import com.openjar.user_service.dto.UserUpdateDto;
import org.springframework.data.domain.Page;

public interface UserService {
    Page<PublicUserDto> getAllUsers(int page, int size);

    PublicUserDto getPublicUser(String id);

    /** Full profile including email: only for the user themself or trusted internal callers. */
    UserResponseDto getUserById(String id);

    UserResponseDto getUserByEmail(String email);

    void createUser(UserRequestDto requestDto);

    UserResponseDto updateUser(String id, UserUpdateDto request);

    void deleteUser(String id);

    void verifyAccount(String email, String otp);

    void resendOtp(String email);
}
