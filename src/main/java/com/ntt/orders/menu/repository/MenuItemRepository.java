package com.ntt.orders.menu.repository;

import com.ntt.orders.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, String> {
}
