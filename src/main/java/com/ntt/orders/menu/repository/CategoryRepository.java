package com.ntt.orders.menu.repository;

import com.ntt.orders.menu.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {
}
