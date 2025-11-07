package com.ntt.orders.table.entity;

import com.ntt.orders.order.entity.Order;
import com.ntt.orders.shared.common.entity.BaseEntity;
import com.ntt.orders.shared.common.enums.TableStatus;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DinningTable extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String tableNumber;

    @Enumerated(EnumType.STRING)
    private TableStatus tableStatus;

    private String qrCodeUrl;

    @OneToMany(mappedBy = "table")
    private List<Order> orders;
}
