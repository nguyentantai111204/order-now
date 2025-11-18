package com.ntt.orders.order.mapper;

import com.ntt.orders.menu.entity.MenuItem;
import com.ntt.orders.menu.repository.MenuItemRepository;
import com.ntt.orders.order.dto.request.OrderItemRequest;
import com.ntt.orders.order.dto.request.OrderRequest;
import com.ntt.orders.order.dto.response.OrderItemResponse;
import com.ntt.orders.order.dto.response.OrderResponse;
import com.ntt.orders.order.entity.Order;
import com.ntt.orders.order.entity.OrderItem;
import com.ntt.orders.shared.common.enums.OrderStatus;
import com.ntt.orders.shared.common.exception.ResourceNotFoundException;
import com.ntt.orders.table.entity.DinningTable;
import com.ntt.orders.table.repository.TableRepository;
import com.ntt.orders.user.entity.User;
import com.ntt.orders.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderMapper {
    private final TableRepository tableRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;

    public Order toEntity(OrderRequest request) {
        DinningTable table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn với ID: " + request.getTableId()));

        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với ID: " + request.getUserId()));
        }

        List<OrderItem> orderItems = request.getOrderItems().stream()
                .map(this::mapToOrderItem)
                .collect(Collectors.toList());

        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (request.getDiscountAmount() != null) {
            totalAmount = totalAmount.subtract(request.getDiscountAmount());
        }

        Order order = Order.builder()
                .table(table)
                .orderBy(user)
                .orderStatus(request.getOrderStatus() != null ? OrderStatus.valueOf(request.getOrderStatus()) : OrderStatus.PENDING)
                .orderTime(LocalDateTime.now())
                .discountAmount(request.getDiscountAmount())
                .totalAmount(totalAmount.max(BigDecimal.ZERO))
                .loyaltyPoints(totalAmount.divide(BigDecimal.valueOf(100000), BigDecimal.ROUND_DOWN).intValue())
                .build();

        orderItems.forEach(item -> item.setOrder(order));
        order.setOrderItems(orderItems);

        return order;
    }

    private OrderItem mapToOrderItem(OrderItemRequest itemRequest) {
        MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn với ID: " + itemRequest.getMenuItemId()));

        BigDecimal price = menuItem.getPrice();
        BigDecimal total = price.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

        return OrderItem.builder()
                .menuItem(menuItem)
                .quantity(itemRequest.getQuantity())
                .price(price)
                .total(total)
                .build();
    }

    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .tableId(order.getTable().getId())
                .tableNumber(order.getTable().getTableNumber())
                .phoneNumber(order.getOrderBy() != null ? order.getOrderBy().getPhoneNumber() : "Khách vãng lai")
                .orderStatus(order.getOrderStatus())
                .orderTime(order.getOrderTime())
                .completedTime(order.getCompletedTime())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .orderItems(order.getOrderItems() != null
                        ? order.getOrderItems().stream().map(this::toItemResponse).collect(Collectors.toList())
                        : null)
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .menuItemId(item.getMenuItem().getId())
                .menuItemName(item.getMenuItem().getName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .total(item.getTotal())
                .build();
    }

}
