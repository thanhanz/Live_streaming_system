package com.thanhan.livestreaming_system.category.service.impl;

import com.thanhan.livestreaming_system.category.dto.CategoryCreateRequest;
import com.thanhan.livestreaming_system.category.dto.CategoryMapper;
import com.thanhan.livestreaming_system.category.dto.CategoryResponse;
import com.thanhan.livestreaming_system.category.entity.Category;
import com.thanhan.livestreaming_system.category.repository.CategoryRepository;
import com.thanhan.livestreaming_system.category.service.CategoryService;
import com.thanhan.livestreaming_system.common.exception.AppException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        Optional<Category> existedCate = categoryRepository.findByTitle(request.title());
        if (existedCate.isPresent()) {
            throw new RuntimeException("Category with title: " + request.title() + " already exists");
        }
        Category category = new Category();
        category.setTitle(request.title());
        return CategoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    public Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new EntityNotFoundException("Category with id: " + categoryId + " not found"));
    }

    @Override
    public CategoryResponse findByName(String name) {
        return  CategoryMapper.toCategoryResponse(categoryRepository.findByTitle(name).orElseThrow(() -> new EntityNotFoundException("Category not found: " + name)));
    }

    @Override
    public void deleteByName(String name) {
        Category c = categoryRepository.findByTitle(name).orElseThrow(() -> new EntityNotFoundException("Category not found: " + name));
        categoryRepository.delete(c);
    }

    @Override
    public CategoryResponse updateCategory(String title, Long categoryId) {
        Category c = categoryRepository.findById(categoryId).orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryId));
        c.setTitle(title);
        return  CategoryMapper.toCategoryResponse(categoryRepository.save(c));
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(CategoryMapper::toCategoryResponse).toList();
    }
}
