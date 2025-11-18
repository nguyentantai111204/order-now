package com.ntt.orders.order.dto.response;

import com.ntt.orders.shared.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private String id;
    private String tableId;
    private String tableNumber;
    private String phoneNumber;
    private OrderStatus orderStatus;
    private LocalDateTime orderTime;
    private LocalDateTime completedTime;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> orderItems;
}
