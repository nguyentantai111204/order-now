package com.ntt.orders.menu.repository;

import com.ntt.orders.menu.entity.Category;
import com.ntt.orders.shared.common.enums.BaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, String> {
    boolean existsBySlug(String slug);

    @Query("""
        SELECT c FROM Category c
        WHERE (:status IS NULL OR c.status = :status)
          AND (:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<Category> findAllByFilters(
            @Param("status") BaseStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
