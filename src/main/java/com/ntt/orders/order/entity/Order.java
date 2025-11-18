package com.ntt.orders.order.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ntt.orders.table.entity.DinningTable;
import com.ntt.orders.payment.entity.Payment;
import com.ntt.orders.shared.common.entity.BaseEntity;
import com.ntt.orders.shared.common.enums.OrderStatus;
import com.ntt.orders.user.entity.User;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "table_id")
    private DinningTable table;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User orderBy;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private LocalDateTime orderTime;
    private LocalDateTime completedTime;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;

    @Column(nullable = false)
    private Integer loyaltyPoints = 0;
}
