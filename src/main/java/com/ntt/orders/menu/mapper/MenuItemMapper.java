package com.ntt.orders.menu.mapper;

import com.ntt.orders.menu.dto.request.MenuItemRequest;
import com.ntt.orders.menu.dto.response.MenuItemResponse;
import com.ntt.orders.menu.entity.MenuItem;
import org.springframework.stereotype.Component;

@Component
public class MenuItemMapper {
    public MenuItem toEntity(MenuItemRequest request, String image) {
        return MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .image(image)
                .build();
    }

    public MenuItemResponse fromEntity(MenuItem menuItem) {
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .image(menuItem.getImage())
                .price(menuItem.getPrice())
                .category(menuItem.getCategory() != null ? menuItem.getCategory().getName() : null)
                .build();
    }
}
