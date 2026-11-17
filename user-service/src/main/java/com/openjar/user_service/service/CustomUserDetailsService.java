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
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String email = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Bad credentials"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserEmail())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .disabled(!user.isVerifiedAccount())   // unverified users cannot log in
                .build();
    }
}
