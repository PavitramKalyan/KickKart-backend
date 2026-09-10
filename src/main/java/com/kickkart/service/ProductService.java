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
import java.util.Map;
import java.util.Objects;
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
        return convertToDtoList(products);
    }

    public ProductDto getProductByIdStr(String idStr) {
        Long id = parseId(idStr);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + idStr));
        return convertToDtoList(List.of(product)).get(0);
    }

    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return convertToDtoList(List.of(product)).get(0);
    }

    public List<ProductDto> getProductsByCategoryStr(String categoryIdStr) {
        Long categoryId = parseId(categoryIdStr);
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return convertToDtoList(products);
    }

    public List<ProductDto> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }
        List<Product> products = productRepository.searchProducts(query.trim());
        return convertToDtoList(products);
    }

    public ProductDto convertToDto(Product product) {
        return convertToDtoList(List.of(product)).get(0);
    }

    private List<ProductDto> convertToDtoList(List<Product> products) {
        if (products.isEmpty()) {
            return List.of();
        }

        List<Long> productIds = products.stream()
                .map(Product::getProductId)
                .collect(Collectors.toList());

        List<Long> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Category> categoryMap = categoryIds.isEmpty()
                ? Map.of()
                : categoryRepository.findAllById(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getCategoryId, c -> c));

        Map<Long, ProductImage> imageMap = productImageRepository.findByProductIdIn(productIds).stream()
                .collect(Collectors.toMap(ProductImage::getProductId, img -> img, (existing, replacement) -> existing));

        return products.stream()
                .map(p -> convertToDto(p, categoryMap.get(p.getCategoryId()), imageMap.get(p.getProductId())))
                .collect(Collectors.toList());
    }

    private ProductDto convertToDto(Product product, Category category, ProductImage image) {
        ProductDto dto = new ProductDto();
        String code = "P" + String.format("%03d", product.getProductId());
        dto.setProductId(code);
        dto.setProductName(product.getName());
        dto.setName(product.getName());
        dto.setBrand(product.getBrand());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setRating(4.5);

        if (product.getCategoryId() != null) {
            String catCode = "C" + String.format("%03d", product.getCategoryId());
            dto.setCategoryId(catCode);
            if (category != null) {
                dto.setCategoryName(category.getCategoryName());
            }
        }

        if (image != null && image.getImageUrl() != null && !image.getImageUrl().trim().isEmpty()) {
            dto.setImageUrl(image.getImageUrl());
        } else {
            dto.setImageUrl("https://ik.imagekit.io/contentcoder/shoes1.jpg");
        }

        return dto;
    }

    public Long parseId(String idStr) {
        if (idStr == null)
            return null;
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
