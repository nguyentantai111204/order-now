package com.ntt.orders.menu.service;

import com.ntt.orders.menu.dto.request.MenuItemRequest;
import com.ntt.orders.menu.dto.response.MenuItemResponse;
import com.ntt.orders.shared.common.dto.PageResponse;
import com.ntt.orders.shared.common.response.ApiResponse;

public interface MenuItemService {
    PageResponse<MenuItemResponse> getAllProducts(String status, String search, int page, int take);
    MenuItemResponse getMenuItemById(String id);
    ApiResponse<MenuItemResponse> createMenuItem(MenuItemRequest dto);
    ApiResponse<MenuItemResponse> updateMenuItem(String id, MenuItemRequest dto);
    ApiResponse<Void> deleteMenuItem(String id);
}
