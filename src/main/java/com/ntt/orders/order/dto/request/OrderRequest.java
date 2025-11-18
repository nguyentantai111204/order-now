package com.ntt.orders.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    @NotNull(message = "Bàn không được để trống")
    private String tableId;
    private String userId;
    private String orderStatus;
    private BigDecimal discountAmount;
    private List<OrderItemRequest> orderItems;
}
