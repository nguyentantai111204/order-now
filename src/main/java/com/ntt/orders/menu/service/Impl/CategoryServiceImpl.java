package com.ntt.orders.menu.service.Impl;

import com.ntt.orders.menu.dto.request.CategoryRequest;
import com.ntt.orders.menu.dto.response.CategoryResponse;
import com.ntt.orders.menu.entity.Category;
import com.ntt.orders.menu.mapper.CategoryMapper;
import com.ntt.orders.menu.repository.CategoryRepository;
import com.ntt.orders.menu.service.CategoryService;
import com.ntt.orders.shared.common.dto.PageResponse;
import com.ntt.orders.shared.common.enums.BaseStatus;
import com.ntt.orders.shared.common.exception.ResourceNotFoundException;
import com.ntt.orders.shared.common.response.ApiResponse;
import com.ntt.orders.shared.common.response.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public PageResponse<CategoryResponse> getAllCategories(String status, String search, int page, int take) {
        Pageable pageable = PageRequest.of(page, take, Sort.by("createdAt").descending());
        Specification<Category> spec = Specification.where(null);

        if (status != null && !status.isEmpty()) {
            BaseStatus baseStatus = BaseStatus.valueOf(status.toUpperCase());
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), baseStatus));
        }

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%")
            );
        }

        Page<CategoryResponse> categoryPage = categoryRepository.findAll(spec, pageable)
                .map(categoryMapper::fromEntity);

        return new PageResponse<>(
                categoryPage.getContent(),
                categoryPage.getNumber(),
                categoryPage.getSize(),
                categoryPage.getTotalElements(),
                categoryPage.getTotalPages(),
                categoryPage.isLast(),
                categoryPage.isFirst(),
                categoryPage.getNumberOfElements()
        );
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
