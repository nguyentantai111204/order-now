package com.ntt.orders.payment.entity;

import com.ntt.orders.order.entity.Order;
import com.ntt.orders.shared.common.entity.BaseEntity;
import com.ntt.orders.shared.common.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private BigDecimal amount;

    private String paymentMethod;

    private LocalDateTime paymentTime;

    private String transactionId;
}
