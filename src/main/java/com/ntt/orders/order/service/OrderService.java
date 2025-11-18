package com.ntt.orders.order.service;

import com.ntt.orders.order.dto.request.OrderRequest;
import com.ntt.orders.order.dto.response.OrderResponse;
import com.ntt.orders.shared.common.dto.PageResponse;
import com.ntt.orders.shared.common.response.ApiResponse;

public interface OrderService {
    ApiResponse<OrderResponse> createOrder(OrderRequest request);
    PageResponse<OrderResponse> getOrders(String status, String search, int page, int take);
    OrderResponse getOrderById(String id);
    ApiResponse<Void> deleteOrder(String id);
}
