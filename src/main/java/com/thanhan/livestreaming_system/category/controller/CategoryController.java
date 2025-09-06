package com.thanhan.livestreaming_system.category.controller;

import com.thanhan.livestreaming_system.category.dto.CategoryCreateRequest;
import com.thanhan.livestreaming_system.category.dto.CategoryResponse;
import com.thanhan.livestreaming_system.category.service.CategoryService;
import com.thanhan.livestreaming_system.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .status(201)
                .message("Get all categories")
                .data(categoryService.getAllCategories())
                .build();
    }

    @GetMapping("/{title}")
    public ApiResponse<CategoryResponse> getCategory(@PathVariable String title) {
        return ApiResponse.<CategoryResponse>builder()
                .status(200)
                .data(categoryService.findByName(title))
                .build();
    }

    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(@RequestBody CategoryCreateRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .status(200)
                .data(categoryService.createCategory(request))
                .build();
    }

    @DeleteMapping("/{title}")
    public ApiResponse<Void> deleteCategory(@PathVariable("title") String title) {
        categoryService.deleteByName(title);
        return ApiResponse.success(204, "Deleted Category");
    }


}
