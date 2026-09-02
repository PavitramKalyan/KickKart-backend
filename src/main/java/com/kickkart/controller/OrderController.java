package com.kickkart.controller;

import com.kickkart.dto.ApiResponse;
import com.kickkart.dto.OrderDto;
import com.kickkart.dto.OrderManagementResponse;
import com.kickkart.dto.OrderRequest;
import com.kickkart.entity.User;
import com.kickkart.service.OrderService;
import com.kickkart.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(Authentication authentication, @Valid @RequestBody OrderRequest request) {
        User user = userService.getEntityByEmail(authentication.getName());
        OrderDto order = orderService.createOrder(user.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Order placed successfully", order));
    }

    @GetMapping
    public ResponseEntity<OrderManagementResponse> getUserOrders(Authentication authentication) {
        User user = userService.getEntityByEmail(authentication.getName());
        OrderManagementResponse response = orderService.getOrderManagementResponse(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(Authentication authentication, @PathVariable String orderId) {
        User user = userService.getEntityByEmail(authentication.getName());
        OrderDto order = orderService.getOrderById(orderId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Order details retrieved", order));
    }
}
