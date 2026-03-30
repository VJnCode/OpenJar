package com.openjar.user_service.service;

import com.openjar.user_service.dto.UserRequestDto;
import com.openjar.user_service.exception.UserAlreadyExistsException;
import com.openjar.user_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    // @Mock creates a fake version of our database repository and password encoder
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    // @InjectMocks injects our fakes into the real UserService so we can test it safely
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_Success_SavesUserWithHashedPassword() {
        // 1. Arrange (Set up our fake data and rules)
        UserRequestDto request = new UserRequestDto();
        request.setUserName("varun_test");
        request.setUserEmail("varun@example.com");
        request.setPassword("plainTextPassword");

        // Tell our fake database to pretend this user does NOT exist yet (return 0)
        when(userRepository.countByUserEmailNative("varun@example.com")).thenReturn(0);
        when(userRepository.countByUserNameNative("varun_test")).thenReturn(0);
        
        // Tell our fake encoder how to behave
        when(passwordEncoder.encode("plainTextPassword")).thenReturn("hashedPassword123");

        // 2. Act (Actually call the method we are testing)
        userService.createUser(request);

        // 3. Assert (Verify that the repository's insert method was called exactly once with our hashed password)
        verify(userRepository, times(1)).insertUserNative("varun_test", "varun@example.com", "hashedPassword123");
    }

    @Test
    void createUser_Fails_WhenEmailAlreadyExists() {
        // 1. Arrange
        UserRequestDto request = new UserRequestDto();
        request.setUserEmail("duplicate@example.com");

        // Force our fake database to say "Wait, I already found 1 user with this email!"
        when(userRepository.countByUserEmailNative("duplicate@example.com")).thenReturn(1);

        // 2 & 3. Act & Assert
        // Verify that calling createUser with this duplicate email throws our exact custom exception
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(request));

        // Extra safety check: Verify that the database INSERT method was NEVER called
        verify(userRepository, never()).insertUserNative(anyString(), anyString(), anyString());
    }
}