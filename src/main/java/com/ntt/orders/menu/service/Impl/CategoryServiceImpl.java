package com.ntt.orders.menu.service.Impl;

import com.ntt.orders.menu.dto.request.CategoryRequest;
import com.ntt.orders.menu.dto.response.CategoryResponse;
import com.ntt.orders.menu.entity.Category;
import com.ntt.orders.menu.mapper.CategoryMapper;
import com.ntt.orders.menu.repository.CategoryRepository;
import com.ntt.orders.menu.service.CategoryService;
import com.ntt.orders.shared.common.enums.BaseStatus;
import com.ntt.orders.shared.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getCategoryById(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return categoryMapper.fromEntity(category);
    }

    @Override
    public ApiResponse<CategoryResponse> createCategory(CategoryRequest dto) {
        Category category = categoryMapper.toEntity(dto);
        categoryRepository.save(category);
        return ApiResponse.created(categoryMapper.fromEntity(category));
    }

    @Override
    public ApiResponse<CategoryResponse> updateCategory(String id, CategoryRequest dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setStatus(dto.getStatus() != null
                ? BaseStatus.valueOf(dto.getStatus().toUpperCase())
                : category.getStatus());
        categoryRepository.save(category);
        return ApiResponse.updated(categoryMapper.fromEntity(category));
    }

    @Override
    public void deleteCategory(String id) {
        categoryRepository.deleteById(id);
    }
}
