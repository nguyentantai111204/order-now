package com.ntt.orders.payment.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MomoResponse {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private BigDecimal amount;
    private Long responseTime;
    private String message;
    private Integer resultCode;
    private String payUrl;
    private String deeplink;
    private String qrCodeUrl;
    private String signature;
}


