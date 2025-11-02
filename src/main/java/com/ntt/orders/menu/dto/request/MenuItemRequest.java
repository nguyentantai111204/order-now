package com.ntt.orders.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MenuItemRequest {

    @NotBlank(message = "Tên món không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Ảnh món không được để trống")
    private MultipartFile image;

    @NotNull(message = "Giá món không được để trống")
    @Positive(message = "Giá món phải lớn hơn 0")
    private BigDecimal price;

    @NotBlank(message = "Danh mục không được để trống")
    private String categoryId;
}
