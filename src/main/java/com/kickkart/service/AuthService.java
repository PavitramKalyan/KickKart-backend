package com.kickkart.service;

import com.kickkart.dto.AuthRequest;
import com.kickkart.dto.AuthResponse;
import com.kickkart.dto.RegisterRequest;
import com.kickkart.entity.JwtToken;
import com.kickkart.entity.User;
import com.kickkart.exception.BadRequestException;
import com.kickkart.repository.JwtTokenRepository;
import com.kickkart.repository.UserRepository;
import com.kickkart.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        String username = request.getEmail().split("@")[0] + "_" + System.currentTimeMillis();
        if (userRepository.existsByUsername(username)) {
            username = "user_" + UUID.randomUUID().toString().substring(0, 8);
        }

        User user = new User();
        user.setUsername(username);
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("CUSTOMER");

        userRepository.save(user);

        return new AuthResponse(null, user.getUserId(), user.getUsername(), user.getEmail(), user.getFullName(), user.getMobileNumber(), user.getRole());
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> userRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email/username or password")));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email/username or password");
        }

        String token = tokenProvider.generateToken(user.getEmail(), user.getUserId());

        // Remove any old token for this user to maintain single active token session if desired
        try {
            jwtTokenRepository.deleteByUserId(user.getUserId());
        } catch (Exception ignored) {}

        // Persist new token in jwt_tokens table
        JwtToken jwtToken = new JwtToken(user.getUserId(), token, LocalDateTime.now().plusDays(1));
        jwtTokenRepository.save(jwtToken);

        return new AuthResponse(token, user.getUserId(), user.getUsername(), user.getEmail(), user.getFullName(), user.getMobileNumber(), user.getRole());
    }

    @Transactional
    public void logout(User user, String tokenStr) {
        try {
            if (tokenStr != null && tokenStr.startsWith("Bearer ")) {
                tokenStr = tokenStr.substring(7);
            }

            if (tokenStr != null && !tokenStr.trim().isEmpty()) {
                jwtTokenRepository.deleteByToken(tokenStr);
            }

            if (user != null && user.getUserId() != null) {
                jwtTokenRepository.deleteByUserId(user.getUserId());
            }
        } catch (Exception e) {
            // Idempotent safe handling for missing or already deleted token
        }
    }

    @Transactional
    public void logout(String tokenStr) {
        logout(null, tokenStr);
    }
}
