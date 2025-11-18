package com.ntt.orders.order.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {
    @NotNull(message = "Món ăn không được để trống")
    private String menuItemId;

    @Positive(message = "Số lượng phải lớn hơn 0")
    private int quantity;

    private BigDecimal price;
}
