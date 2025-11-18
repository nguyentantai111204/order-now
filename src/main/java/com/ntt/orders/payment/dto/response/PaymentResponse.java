package com.ntt.orders.payment.dto.response;

import com.ntt.orders.shared.common.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private String orderId;
    private String transactionId;
    private PaymentStatus status;
    private String message;
}
