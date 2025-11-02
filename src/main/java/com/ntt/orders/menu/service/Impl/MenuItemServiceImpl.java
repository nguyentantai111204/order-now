package com.ntt.orders.menu.service.Impl;

import com.ntt.orders.menu.dto.request.MenuItemRequest;
import com.ntt.orders.menu.dto.response.MenuItemResponse;
import com.ntt.orders.menu.entity.Category;
import com.ntt.orders.menu.entity.MenuItem;
import com.ntt.orders.menu.mapper.MenuItemMapper;
import com.ntt.orders.menu.repository.CategoryRepository;
import com.ntt.orders.menu.repository.MenuItemRepository;
import com.ntt.orders.menu.service.MenuItemService;
import com.ntt.orders.shared.common.dto.PageResponse;
import com.ntt.orders.shared.common.enums.BaseStatus;
import com.ntt.orders.shared.common.exception.BadRequestException;
import com.ntt.orders.shared.common.exception.ResourceNotFoundException;
import com.ntt.orders.shared.common.response.ApiResponse;
import com.ntt.orders.shared.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final CloudinaryService cloudinaryService;
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuItemMapper menuItemMapper;

    @Override
    public PageResponse<MenuItemResponse> getAllProducts(String status, String search, int page, int take) {
        Pageable pageable = PageRequest.of(page, take, Sort.by("createdAt").descending());
        Specification<MenuItem> spec = Specification.where(null);

        if (status != null && !status.isEmpty()) {
            BaseStatus baseStatus = BaseStatus.valueOf(status.toUpperCase());
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), baseStatus));
        }

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
        }

        Page<MenuItemResponse> menuPage = menuItemRepository.findAll(spec, pageable)
                .map(menuItemMapper::fromEntity);

        return new PageResponse<>(
                menuPage.getContent(),
                menuPage.getNumber(),
                menuPage.getSize(),
                menuPage.getTotalElements(),
                menuPage.getTotalPages(),
                menuPage.isLast(),
                menuPage.isFirst(),
                menuPage.getNumberOfElements()
        );
    }

    @Override
    public MenuItemResponse getMenuItemById(String id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn không tồn tại"));

        return menuItemMapper.fromEntity(menuItem);
    }


    @Override
    public ApiResponse<MenuItemResponse> createMenuItem(MenuItemRequest dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Danh mục không tồn tại"));

        String imageUrl = null;
        try {
            if (dto.getImage() != null) {
                imageUrl = cloudinaryService.uploadFile(dto.getImage());
            }
        } catch (IOException e) {
            throw new BadRequestException("Upload ảnh thất bại");
        }

        MenuItem menuItem = menuItemMapper.toEntity(dto,imageUrl);
        menuItem.setCategory(category);
        menuItem.setImage(imageUrl);

        menuItemRepository.save(menuItem);
        return ApiResponse.created(menuItemMapper.fromEntity(menuItem));
    }


    @Override
    public ApiResponse<MenuItemResponse> updateMenuItem(String id, MenuItemRequest dto) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn không tồn tại"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Danh mục không tồn tại"));

        try {
            if (dto.getImage() != null) {
                String imageUrl = cloudinaryService.uploadFile(dto.getImage());
                menuItem.setImage(imageUrl);
            }
        } catch (IOException e) {
            throw new BadRequestException("Upload ảnh thất bại");
        }

        menuItem.setName(dto.getName());
        menuItem.setDescription(dto.getDescription());
        menuItem.setCategory(category);
        menuItem.setPrice(dto.getPrice());

        menuItemRepository.save(menuItem);
        return ApiResponse.updated(menuItemMapper.fromEntity(menuItem));
    }


    @Override
    public ApiResponse<Void> deleteMenuItem(String id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn không tồn tại"));
        menuItemRepository.delete(menuItem);
        return ApiResponse.deleted();
    }
}

