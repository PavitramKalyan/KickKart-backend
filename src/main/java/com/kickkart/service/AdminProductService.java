package com.kickkart.service;

import com.kickkart.dto.AdminProductRequest;
import com.kickkart.dto.ProductDto;
import com.kickkart.entity.Category;
import com.kickkart.entity.Product;
import com.kickkart.entity.ProductImage;
import com.kickkart.exception.BadRequestException;
import com.kickkart.exception.ResourceNotFoundException;
import com.kickkart.repository.CategoryRepository;
import com.kickkart.repository.ProductImageRepository;
import com.kickkart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AdminProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductService productService;

    @Transactional
    public ProductDto addProduct(AdminProductRequest request) {
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ONE) < 0) {
            throw new BadRequestException("Price must be at least ₹1.00");
        }

        if (request.getStock() == null || request.getStock() < 0) {
            throw new BadRequestException("Stock quantity cannot be negative");
        }

        // Validate Category Existence
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Selected category ID " + request.getCategoryId() + " does not exist"));

        // Validate Duplicate Product Name
        if (productRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("A product with the name '" + request.getName().trim() + "' already exists");
        }

        Product product = new Product();
        product.setName(request.getName().trim());
        product.setBrand(request.getBrand() != null ? request.getBrand().trim() : "KickKart");
        product.setDescription(request.getDescription().trim());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategoryId(category.getCategoryId());

        product = productRepository.save(product);

        // Save Image URL
        String imgUrl = (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty())
                ? request.getImageUrl().trim()
                : "https://ik.imagekit.io/contentcoder/shoes1.jpg";

        ProductImage productImage = new ProductImage(product.getProductId(), imgUrl);
        productImageRepository.save(productImage);

        return productService.convertToDto(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        try {
            productImageRepository.findFirstByProductId(productId).ifPresent(productImageRepository::delete);
            productRepository.delete(product);
        } catch (Exception e) {
            // Soft delete fall-back if product is referenced by historical orders
            product.setStock(0);
            productRepository.save(product);
        }
    }
}
