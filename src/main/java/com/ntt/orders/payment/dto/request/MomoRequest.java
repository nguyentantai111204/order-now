package com.ntt.orders.payment.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MomoRequest {
    private String id;
    private BigDecimal amount;
    private String orderId;
}
