package com.ntt.orders.menu.service.Impl;

import com.ntt.orders.menu.dto.request.CategoryRequest;
import com.ntt.orders.menu.dto.response.CategoryResponse;
import com.ntt.orders.menu.entity.Category;
import com.ntt.orders.menu.mapper.CategoryMapper;
import com.ntt.orders.menu.repository.CategoryRepository;
import com.ntt.orders.menu.service.CategoryService;
import com.ntt.orders.shared.common.enums.BaseStatus;
import com.ntt.orders.shared.common.exception.BadRequestException;
import com.ntt.orders.shared.common.exception.ResourceNotFoundException;
import com.ntt.orders.shared.common.response.ApiResponse;
import com.ntt.orders.shared.common.response.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> getAllCategories(String status, String search, int page, int take) {
        Pageable pageable = PageRequest.of(page, take);

        // Nếu không có params thì trả về tất cả
        boolean noParams = (status == null || status.isEmpty()) && (search == null || search.isEmpty());
        if (noParams) {
            return categoryRepository.findAll(pageable)
                    .map(categoryMapper::fromEntity)
                    .toList();
        }

        BaseStatus baseStatus = null;
        if (status != null && !status.isEmpty()) {
            baseStatus = BaseStatus.valueOf(status.toUpperCase());
        }

        return categoryRepository.findAllByFilters(baseStatus, search, pageable)
                .map(categoryMapper::fromEntity)
                .toList();
    }

    @Override
    public CategoryResponse getCategoryById(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy category id: "+ id));
        return categoryMapper.fromEntity(category);
    }

    @Override
    public ApiResponse<CategoryResponse> createCategory(CategoryRequest dto) {
        if(categoryRepository.existsBySlug(dto.getSlug()))
            return ApiResponse.error("Slug đã tồn tại", ResponseCode.DUPLICATE_ENTRY, null);
        Category category = categoryMapper.toEntity(dto);
        category.setCreatedAt(java.time.LocalDateTime.now());
        categoryRepository.save(category);
        return ApiResponse.created(categoryMapper.fromEntity(category));
    }

    @Override
    public ApiResponse<CategoryResponse> updateCategory(String id, CategoryRequest dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy category id: "+ id));
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setStatus(dto.getStatus() != null
                ? BaseStatus.valueOf(dto.getStatus().toUpperCase())
                : category.getStatus());
        category.setUpdatedAt(java.time.LocalDateTime.now());
        categoryRepository.save(category);
        return ApiResponse.updated(categoryMapper.fromEntity(category));
    }

    @Override
    public ApiResponse<Void> deleteCategory(String id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy category id: " + id);
        }
        categoryRepository.deleteById(id);
        return ApiResponse.deleted();
    }

}
