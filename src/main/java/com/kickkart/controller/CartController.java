package com.kickkart.controller;

import com.kickkart.dto.ApiResponse;
import com.kickkart.dto.CartItemRequest;
import com.kickkart.dto.CartResponse;
import com.kickkart.entity.User;
import com.kickkart.service.CartService;
import com.kickkart.service.ProductService;
import com.kickkart.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        User user = userService.getEntityByEmail(authentication.getName());
        CartResponse cart = cartService.getCart(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(Authentication authentication, @Valid @RequestBody CartItemRequest request) {
        User user = userService.getEntityByEmail(authentication.getName());
        CartResponse cart = cartService.addToCart(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            Authentication authentication,
            @PathVariable String productId,
            @RequestParam(name = "quantity") Integer quantity) {
        User user = userService.getEntityByEmail(authentication.getName());
        Long pId = productService.parseId(productId);
        CartResponse cart = cartService.updateCartItem(user.getUserId(), pId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", cart));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(Authentication authentication, @PathVariable String productId) {
        User user = userService.getEntityByEmail(authentication.getName());
        Long pId = productService.parseId(productId);
        CartResponse cart = cartService.removeCartItem(user.getUserId(), pId);
        return ResponseEntity.ok(ApiResponse.success("Cart item removed", cart));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication authentication) {
        User user = userService.getEntityByEmail(authentication.getName());
        cartService.clearCart(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared"));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getCartCount(Authentication authentication) {
        User user = userService.getEntityByEmail(authentication.getName());
        Integer count = cartService.getCartCount(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Cart count retrieved", count));
    }
}
