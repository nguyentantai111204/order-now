package com.ntt.orders.order.repository;

import com.ntt.orders.menu.entity.Category;
import com.ntt.orders.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {
}
