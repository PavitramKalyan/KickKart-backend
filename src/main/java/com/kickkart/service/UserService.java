package com.kickkart.service;

import com.kickkart.dto.ChangePasswordRequest;
import com.kickkart.dto.UserDto;
import com.kickkart.entity.User;
import com.kickkart.exception.BadRequestException;
import com.kickkart.exception.ResourceNotFoundException;
import com.kickkart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.findByUsername(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email)));
        return convertToDto(user);
    }

    public UserDto updateCurrentUser(String email, UserDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.findByUsername(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email)));

        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getMobileNumber() != null) {
            user.setMobileNumber(dto.getMobileNumber());
        }
        if (dto.getProfilePicture() != null) {
            user.setProfilePicture(dto.getProfilePicture());
        }

        userRepository.save(user);
        return convertToDto(user);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.findByUsername(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email)));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public User getEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.findByUsername(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email)));
    }

    private UserDto convertToDto(User user) {
        return new UserDto(
                user.getUserId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getProfilePicture(),
                user.getRole()
        );
    }
}
