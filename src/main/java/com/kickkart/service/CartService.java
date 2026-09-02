package com.kickkart.service;

import com.kickkart.dto.CartItemDto;
import com.kickkart.dto.CartItemRequest;
import com.kickkart.dto.CartResponse;
import com.kickkart.dto.ProductDto;
import com.kickkart.entity.CartItem;
import com.kickkart.entity.Product;
import com.kickkart.exception.BadRequestException;
import com.kickkart.exception.ResourceNotFoundException;
import com.kickkart.repository.CartRepository;
import com.kickkart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    public CartResponse getCart(Long userId) {
        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        List<CartItemDto> dtoList = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItems = 0;

        for (CartItem item : cartItems) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                ProductDto productDto = productService.convertToDto(product);
                CartItemDto dto = new CartItemDto();
                dto.setId(item.getId());
                dto.setProductId(product.getProductId());
                dto.setProductCode(productDto.getProductId());
                dto.setProductName(product.getName());
                dto.setBrand(product.getBrand());
                dto.setPrice(product.getPrice());
                dto.setStock(product.getStock());
                dto.setImageUrl(productDto.getImageUrl());
                dto.setQuantity(item.getQuantity());

                BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                dto.setSubtotal(itemSubtotal);

                dtoList.add(dto);
                subtotal = subtotal.add(itemSubtotal);
                totalItems += item.getQuantity();
            }
        }

        BigDecimal gst = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = subtotal.add(gst).setScale(2, RoundingMode.HALF_UP);

        return new CartResponse(dtoList, subtotal, gst, grandTotal, totalItems);
    }

    public CartResponse addToCart(Long userId, CartItemRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        if (product.getStock() <= 0) {
            throw new BadRequestException("Product is out of stock");
        }

        Optional<CartItem> existingOpt = cartRepository.findByUserIdAndProductId(userId, request.getProductId());
        int newQuantity = request.getQuantity();

        if (existingOpt.isPresent()) {
            CartItem existing = existingOpt.get();
            newQuantity = existing.getQuantity() + request.getQuantity();
            if (newQuantity > product.getStock()) {
                throw new BadRequestException("Requested quantity (" + newQuantity + ") exceeds available stock (" + product.getStock() + ")");
            }
            existing.setQuantity(newQuantity);
            cartRepository.save(existing);
        } else {
            if (newQuantity > product.getStock()) {
                throw new BadRequestException("Requested quantity (" + newQuantity + ") exceeds available stock (" + product.getStock() + ")");
            }
            CartItem newItem = new CartItem(userId, request.getProductId(), newQuantity);
            cartRepository.save(newItem);
        }

        return getCart(userId);
    }

    public CartResponse updateCartItem(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            return removeCartItem(userId, productId);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (quantity > product.getStock()) {
            throw new BadRequestException("Requested quantity exceeds available stock (" + product.getStock() + ")");
        }

        CartItem cartItem = cartRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        cartItem.setQuantity(quantity);
        cartRepository.save(cartItem);

        return getCart(userId);
    }

    public CartResponse removeCartItem(Long userId, Long productId) {
        cartRepository.findByUserIdAndProductId(userId, productId).ifPresent(cartRepository::delete);
        return getCart(userId);
    }

    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
    }

    public Integer getCartCount(Long userId) {
        List<CartItem> items = cartRepository.findByUserId(userId);
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }
}
