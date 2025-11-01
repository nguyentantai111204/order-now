package com.ntt.orders.menu.mapper;

import com.ntt.orders.menu.dto.request.CategoryRequest;
import com.ntt.orders.menu.dto.response.CategoryResponse;
import com.ntt.orders.menu.entity.Category;
import com.ntt.orders.shared.common.enums.BaseStatus;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public Category toEntity(CategoryRequest request) {
        return Category.builder()
                .status(BaseStatus.valueOf(request.getStatus().toUpperCase()))
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    public CategoryResponse fromEntity(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .status(category.getStatus().name())
                .slug(category.getSlug())
                .build();
    }



}
