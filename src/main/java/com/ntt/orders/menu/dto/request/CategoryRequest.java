package com.ntt.orders.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "Trạng thái không được để trống")
    @Pattern(regexp = "ACTIVE|INACTIVE|DELETED", message = "Trạng thái phải là ACTIVE, INACTIVE hoặc DELETED")
    private String status;

    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    private String description;

    @NotBlank(message = "Slug không được để trống")
    private String slug;
}
