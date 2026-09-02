package com.kickkart.controller;

import com.kickkart.dto.ApiResponse;
import com.kickkart.dto.AuthRequest;
import com.kickkart.dto.AuthResponse;
import com.kickkart.dto.RegisterRequest;
import com.kickkart.entity.User;
import com.kickkart.repository.UserRepository;
import com.kickkart.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful. Please login.", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        try {
            String authHeader = request.getHeader("Authorization");
            User user = null;
            if (authentication != null && authentication.getName() != null) {
                user = userRepository.findByEmail(authentication.getName()).orElse(null);
            }

            authService.logout(user, authHeader);

            // Clear authentication cookies from browser
            String[] cookieNames = {"kickkart_token", "jwt", "token", "JSESSIONID"};
            for (String cookieName : cookieNames) {
                Cookie cookie = new Cookie(cookieName, null);
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }

            return ResponseEntity.ok(ApiResponse.success("Logout successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Logout failed"));
        }
    }
}
