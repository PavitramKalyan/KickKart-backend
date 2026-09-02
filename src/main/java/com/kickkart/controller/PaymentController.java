package com.kickkart.controller;

import com.kickkart.dto.ApiResponse;
import com.kickkart.dto.OrderDto;
import com.kickkart.dto.RazorpayOrderRequest;
import com.kickkart.dto.RazorpayOrderResponse;
import com.kickkart.dto.RazorpayVerificationRequest;
import com.kickkart.entity.User;
import com.kickkart.repository.UserRepository;
import com.kickkart.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @Value("${razorpay.key.id:rzp_test_TOorQ6SD8YpZfV}")
    private String razorpayKeyId;

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<RazorpayOrderResponse>> createRazorpayOrder(
            Authentication authentication,
            @Valid @RequestBody RazorpayOrderRequest request) {

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        RazorpayOrderResponse response = paymentService.createRazorpayOrder(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Razorpay order created successfully", response));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<OrderDto>> verifyPayment(
            Authentication authentication,
            @Valid @RequestBody RazorpayVerificationRequest request) {

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        OrderDto order = paymentService.verifyAndCompletePayment(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Payment verified and order created successfully", order));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature,
            @RequestBody String payload) {
        // Idempotent webhook listener endpoint
        return ResponseEntity.ok("Webhook received");
    }
}
