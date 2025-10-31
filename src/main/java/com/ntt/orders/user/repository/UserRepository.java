package com.ntt.orders.user.repository;

import com.ntt.orders.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByRefreshToken(String refreshToken);

    boolean existsByPhoneNumber(String phoneNumber);
}
