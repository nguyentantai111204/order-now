package com.ntt.orders.config;

import com.ntt.orders.auth.service.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ===== PUBLIC - Không cần đăng nhập =====
                        // Auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Menu - Xem menu public
                        .requestMatchers(HttpMethod.GET, "/api/menu/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

                        // Table - Quét QR code
                        .requestMatchers(HttpMethod.GET, "/api/tables/*/qr").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tables/*/info").permitAll()

                        // Order - KHÁCH VÃNG LAI có thể order
                        .requestMatchers(HttpMethod.POST, "/api/orders/guest").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/orders/*/status").permitAll()

                        // Payment - Callback từ payment gateway
                        .requestMatchers("/api/payments/callback/**").permitAll()

                        // ===== CUSTOMER - Cần đăng nhập =====
                        .requestMatchers("/api/orders/my-orders").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/orders").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")

                        // Loyalty - Xem điểm
                        .requestMatchers("/api/loyalty/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")

                        // ===== STAFF - Nhân viên phục vụ =====
                        .requestMatchers("/api/orders/*/update-status").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/orders/pending").hasAnyRole("STAFF", "ADMIN")

                        // ===== ADMIN - Quản lý =====
                        .requestMatchers(HttpMethod.POST, "/api/menu/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/menu/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/menu/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")

                        .requestMatchers("/api/tables/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/reports/**").hasRole("ADMIN")

                        // Tất cả request khác cần authenticate
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000"

        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}