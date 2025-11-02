package com.ntt.orders.menu.service;

import com.ntt.orders.menu.dto.request.CategoryRequest;
import com.ntt.orders.menu.dto.response.CategoryResponse;
import com.ntt.orders.shared.common.response.ApiResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories(String status, String search, int page, int take);
    CategoryResponse getCategoryById(String id);
    ApiResponse<CategoryResponse> createCategory(CategoryRequest dto);
    ApiResponse<CategoryResponse> updateCategory(String id, CategoryRequest dto);
    ApiResponse<Void> deleteCategory(String id);
}
