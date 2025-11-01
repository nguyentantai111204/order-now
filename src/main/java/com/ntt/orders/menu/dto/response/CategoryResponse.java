package com.ntt.orders.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private String id;
    private String name;
    private String status;
    private String description;
    private String slug;
}
