package com.kickkart.controller;

import com.kickkart.dto.ApiResponse;
import com.kickkart.dto.ChangePasswordRequest;
import com.kickkart.dto.UserDto;
import com.kickkart.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(Authentication authentication) {
        UserDto user = userService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("User details retrieved", user));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> updateCurrentUser(Authentication authentication, @RequestBody UserDto dto) {
        UserDto user = userService.updateCurrentUser(authentication.getName(), dto);
        return ResponseEntity.ok(ApiResponse.success("User profile updated", user));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }
}
