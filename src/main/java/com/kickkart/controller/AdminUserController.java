package com.kickkart.controller;

import com.kickkart.dto.AdminUserDto;
import com.kickkart.dto.AdminUserUpdateRequest;
import com.kickkart.dto.ApiResponse;
import com.kickkart.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminUserDto>>> getAllUsers() {
        List<AdminUserDto> users = adminUserService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<AdminUserDto>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        AdminUserDto updatedUser = adminUserService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
    }
}
