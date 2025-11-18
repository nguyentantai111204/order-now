package com.ntt.orders.order.service.Impl;

import com.ntt.orders.order.dto.request.OrderRequest;
import com.ntt.orders.order.dto.response.OrderResponse;
import com.ntt.orders.order.entity.Order;
import com.ntt.orders.order.mapper.OrderMapper;
import com.ntt.orders.order.repository.OrderRepository;
import com.ntt.orders.order.service.OrderService;
import com.ntt.orders.payment.dto.request.MomoRequest;
import com.ntt.orders.payment.dto.response.MomoResponse;
import com.ntt.orders.shared.common.dto.PageResponse;
import com.ntt.orders.shared.common.enums.BaseStatus;
import com.ntt.orders.shared.common.exception.ResourceNotFoundException;
import com.ntt.orders.shared.common.response.ApiResponse;
import com.ntt.orders.shared.service.MomoService;
import com.ntt.orders.table.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final TableRepository tableRepository;
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final MomoService momoPaymentService;

    @Override
    public ApiResponse<OrderResponse> createOrder(OrderRequest request) {
        var table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tồn tại table id: "+request.getTableId()));

        Order order = orderMapper.toEntity(request);

        BigDecimal total = order.getOrderItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        orderRepository.save(order);

        return ApiResponse.success(orderMapper.toResponse(order));
    }

    @Override
    public PageResponse<OrderResponse> getOrders(String status, String search, int page, int take) {
        Pageable pageable = PageRequest.of(page, take, Sort.by("createdAt").descending());
        Specification<Order> spec = Specification.where(null);

        if (status != null && !status.isEmpty()) {
            BaseStatus baseStatus = BaseStatus.valueOf(status.toUpperCase());
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), baseStatus));
        }

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("id")), "%" + search.toLowerCase() + "%")
            );
        }

        Page<OrderResponse> pageResult = orderRepository.findAll(spec, pageable)
                .map(orderMapper::toResponse);

        return new PageResponse<>(
                pageResult.getContent(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast(),
                pageResult.isFirst(),
                pageResult.getNumberOfElements()
        );
    }

    @Override
    public OrderResponse getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy order id: " + id));
        return orderMapper.toResponse(order);
    }

    @Override
    public ApiResponse<Void> deleteOrder(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy order id: " + id));

        orderRepository.delete(order);
        return ApiResponse.deleted();
    }
    public MomoResponse processPayment(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        MomoRequest paymentRequest = new MomoRequest();
        paymentRequest.setOrderId(orderId);
        paymentRequest.setAmount(order.getTotalAmount());
        paymentRequest.setOrderId("Payment for order #" + orderId);

        return momoPaymentService.createPayment(paymentRequest);
    }



}
