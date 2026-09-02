package com.kickkart.controller;

import com.kickkart.dto.ApiResponse;
import com.kickkart.dto.ProductDto;
import com.kickkart.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAllProducts() {
        List<ProductDto> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDto>>> searchProducts(@RequestParam(name = "query", required = false, defaultValue = "") String query) {
        List<ProductDto> products = productService.searchProducts(query);
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved", products));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getProductsByCategory(@PathVariable String categoryId) {
        List<ProductDto> products = productService.getProductsByCategoryStr(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved by category", products));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@PathVariable String productId) {
        ProductDto product = productService.getProductByIdStr(productId);
        return ResponseEntity.ok(ApiResponse.success("Product details retrieved", product));
    }
}
