package com.ntt.orders.menu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryRequest {
    private String status;
    private String name;
    private String description;
    private String slug;
}
