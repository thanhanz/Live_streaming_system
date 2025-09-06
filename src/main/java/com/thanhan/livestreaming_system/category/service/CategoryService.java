package com.thanhan.livestreaming_system.category.service;

import com.thanhan.livestreaming_system.category.dto.CategoryCreateRequest;
import com.thanhan.livestreaming_system.category.dto.CategoryResponse;
import com.thanhan.livestreaming_system.category.entity.Category;

import java.util.List;

public interface CategoryService {
    CategoryResponse findByName(String name);
    void deleteByName(String name);
    CategoryResponse updateCategory(String title, Long categoryId);
    List<CategoryResponse> getAllCategories();
    CategoryResponse createCategory(CategoryCreateRequest request);
    Category findCategoryById(Long categoryId);
}
