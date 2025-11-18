package com.ntt.orders.order.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private String menuItemId;
    private String menuItemName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal total;
}
