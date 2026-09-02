package com.kickkart.service;

import com.kickkart.dto.ProductDto;
import com.kickkart.entity.Category;
import com.kickkart.entity.Product;
import com.kickkart.entity.ProductImage;
import com.kickkart.exception.ResourceNotFoundException;
import com.kickkart.repository.CategoryRepository;
import com.kickkart.repository.ProductImageRepository;
import com.kickkart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    public List<ProductDto> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public ProductDto getProductByIdStr(String idStr) {
        Long id = parseId(idStr);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + idStr));
        return convertToDto(product);
    }

    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return convertToDto(product);
    }

    public List<ProductDto> getProductsByCategoryStr(String categoryIdStr) {
        Long categoryId = parseId(categoryIdStr);
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return products.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<ProductDto> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }
        List<Product> products = productRepository.searchProducts(query.trim());
        return products.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        String code = "P" + String.format("%03d", product.getProductId());
        dto.setProductId(code);
        dto.setProductName(product.getName());
        dto.setName(product.getName());
        dto.setBrand(product.getBrand());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setRating(4.5); // Dynamic default rating

        if (product.getCategoryId() != null) {
            String catCode = "C" + String.format("%03d", product.getCategoryId());
            dto.setCategoryId(catCode);

            Category category = categoryRepository.findById(product.getCategoryId()).orElse(null);
            if (category != null) {
                dto.setCategoryName(category.getCategoryName());
            }
        }

        ProductImage image = productImageRepository.findFirstByProductId(product.getProductId()).orElse(null);
        if (image != null && image.getImageUrl() != null && !image.getImageUrl().trim().isEmpty()) {
            dto.setImageUrl(image.getImageUrl());
        } else {
            dto.setImageUrl("https://ik.imagekit.io/contentcoder/shoes1.jpg");
        }

        return dto;
    }

    public Long parseId(String idStr) {
        if (idStr == null) return null;
        String clean = idStr.replaceAll("(?i)^[PC0]+", "");
        try {
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            try {
                return Long.parseLong(idStr);
            } catch (NumberFormatException ex) {
                throw new ResourceNotFoundException("Invalid ID format: " + idStr);
            }
        }
    }
}
