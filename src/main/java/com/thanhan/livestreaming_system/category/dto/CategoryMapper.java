package com.thanhan.livestreaming_system.category.dto;

import com.thanhan.livestreaming_system.category.entity.Category;

public class CategoryMapper {

    public static CategoryResponse toCategoryResponse(Category category) {
            return new CategoryResponse(category.getId(), category.getTitle());
    }
}
