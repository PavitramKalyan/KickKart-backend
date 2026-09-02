package com.kickkart.controller;

import com.kickkart.dto.AdminProductRequest;
import com.kickkart.dto.ApiResponse;
import com.kickkart.dto.ProductDto;
import com.kickkart.exception.BadRequestException;
import com.kickkart.service.AdminProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    @Autowired
    private AdminProductService adminProductService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto>> addProduct(@Valid @RequestBody AdminProductRequest request) {
        ProductDto product = adminProductService.addProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product added successfully", product));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String productId) {
        try {
            String cleanIdStr = productId.toUpperCase().replaceAll("^[P0]+", "");
            if (cleanIdStr.isEmpty()) cleanIdStr = "0";
            Long cleanId = Long.parseLong(cleanIdStr);
            adminProductService.deleteProduct(cleanId);
            return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid product ID format: " + productId);
        }
    }
}
