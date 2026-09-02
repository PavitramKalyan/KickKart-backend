package com.kickkart.controller;

import com.kickkart.dto.ApiResponse;
import com.kickkart.entity.Category;
import com.kickkart.service.CategoryService;
import com.kickkart.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable String categoryId) {
        Long id = productService.parseId(categoryId);
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category));
    }
}
