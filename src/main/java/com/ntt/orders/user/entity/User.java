package com.ntt.orders.user.entity;

import com.ntt.orders.shared.common.entity.BaseEntity;
import com.ntt.orders.shared.common.enums.UserRole;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(length = 15)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    private LocalDateTime lastLoginAt;

    private String refreshToken;


}
