package com.openjar.user_service.service;

import com.openjar.user_service.models.User;
import com.openjar.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user found with email: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserEmail())   // ← userEmail is the login identifier
                .password(user.getPassword())    // ← already BCrypt encoded from registration
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .disabled(!user.isVerified())    // ← unverified users cannot login
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }
}