package com.ntt.orders.menu.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class MenuItemResponse {
    private String id;
    private String name;
    private String description;
    private String image;
    private BigDecimal price;
    private String category;
}
