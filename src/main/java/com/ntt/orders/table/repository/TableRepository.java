package com.ntt.orders.table.repository;

import com.ntt.orders.menu.entity.Category;
import com.ntt.orders.table.entity.DinningTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TableRepository extends JpaRepository<DinningTable, String>, JpaSpecificationExecutor<DinningTable> {
    boolean existsByTableNumber(String tableNumber);
    Optional<DinningTable> findByTableNumber(String tableNumber);
}
