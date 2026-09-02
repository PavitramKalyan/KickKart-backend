package com.kickkart.service;

import com.kickkart.dto.AdminUserDto;
import com.kickkart.dto.AdminUserUpdateRequest;
import com.kickkart.entity.User;
import com.kickkart.exception.BadRequestException;
import com.kickkart.exception.ResourceNotFoundException;
import com.kickkart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<AdminUserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new AdminUserDto(
                        user.getUserId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getMobileNumber(),
                        user.getRole(),
                        user.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminUserDto updateUser(Long userId, AdminUserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Duplicate username check if username changed
        if (!user.getUsername().equalsIgnoreCase(request.getUsername().trim())) {
            if (userRepository.existsByUsername(request.getUsername().trim())) {
                throw new BadRequestException("Username '" + request.getUsername().trim() + "' is already taken");
            }
            user.setUsername(request.getUsername().trim());
        }

        // Duplicate email check if email changed
        if (!user.getEmail().equalsIgnoreCase(request.getEmail().trim())) {
            if (userRepository.existsByEmail(request.getEmail().trim())) {
                throw new BadRequestException("Email '" + request.getEmail().trim() + "' is already in use");
            }
            user.setEmail(request.getEmail().trim());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName().trim());
        }

        if (request.getMobileNumber() != null) {
            user.setMobileNumber(request.getMobileNumber().trim());
        }

        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            String roleUpper = request.getRole().trim().toUpperCase();
            if (!"CUSTOMER".equals(roleUpper) && !"ADMIN".equals(roleUpper)) {
                throw new BadRequestException("Role must be either CUSTOMER or ADMIN");
            }
            user.setRole(roleUpper);
        }

        // Optional password update (BCrypt encoded)
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            if (request.getPassword().trim().length() < 6) {
                throw new BadRequestException("Password must be at least 6 characters long");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        }

        userRepository.save(user);

        return new AdminUserDto(
                user.getUserId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
