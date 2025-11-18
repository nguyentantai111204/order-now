package com.ntt.orders.payment.repository;

import com.ntt.orders.order.entity.Order;
import com.ntt.orders.payment.entity.Payment;
import com.ntt.orders.shared.common.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByTransactionId(String transactionId);

    @Query("SELECT p.paymentStatus FROM Payment p WHERE p.order.id = :orderId")
    Optional<PaymentStatus> findPaymentStatusByOrderId(@Param("orderId") Long orderId);
}
