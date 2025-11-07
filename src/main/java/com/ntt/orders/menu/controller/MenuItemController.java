package com.ntt.orders.menu.controller;

import com.ntt.orders.menu.dto.request.MenuItemRequest;
import com.ntt.orders.menu.dto.response.MenuItemResponse;
import com.ntt.orders.menu.service.MenuItemService;
import com.ntt.orders.shared.common.dto.PageResponse;
import com.ntt.orders.shared.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @GetMapping
    public ResponseEntity<PageResponse<MenuItemResponse>> getAllProducts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int take
    ) {
        PageResponse<MenuItemResponse> response = menuItemService.getAllProducts(status, search, page, take);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponse> getMenuItemById(@PathVariable String id) {
        MenuItemResponse response = menuItemService.getMenuItemById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MenuItemResponse>> createMenuItem(
            @Valid @ModelAttribute MenuItemRequest request
    ) {
        ApiResponse<MenuItemResponse> response = menuItemService.createMenuItem(request);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateMenuItem(
            @PathVariable String id,
            @Valid @ModelAttribute MenuItemRequest request
    ) {
        ApiResponse<MenuItemResponse> response = menuItemService.updateMenuItem(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable String id) {
        ApiResponse<Void> response = menuItemService.deleteMenuItem(id);
        return ResponseEntity.ok(response);
    }
}
