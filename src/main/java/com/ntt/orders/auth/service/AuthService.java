package com.ntt.orders.auth.service;

import com.ntt.orders.auth.dto.request.LoginRequest;
import com.ntt.orders.auth.dto.request.RegisterRequest;
import com.ntt.orders.auth.dto.response.AuthResponse;
import com.ntt.orders.shared.common.exception.BadRequestException;
import com.ntt.orders.user.entity.User;
import com.ntt.orders.user.repository.UserRepository;
import com.ntt.orders.shared.common.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        logger.info("Registration attempt for phone: {}", request.getPhoneNumber());

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Số điện thoại đã tồn tại!");
        }

        // Validate password strength
        if (request.getPassword().length() < 6) {
            throw new BadRequestException("Mật khẩu phải có ít nhất 6 ký tự");
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .phoneNumber(request.getPhoneNumber().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .build();

        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        logger.info("User registered successfully: {}", user.getPhoneNumber());

        return new AuthResponse(accessToken, refreshToken, user.getFullName(), user.getRole().name());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        logger.info("Login attempt for phone: {}", request.getPhoneNumber());

        try {
            // QUAN TRỌNG: Authenticate trước khi tìm user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getPhoneNumber().trim(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Sau khi xác thực thành công mới lấy user
            User user = userRepository.findByPhoneNumber(request.getPhoneNumber().trim())
                    .orElseThrow(() -> new BadRequestException("Người dùng không tồn tại"));

            // Update last login
            user.setLastLoginAt(java.time.LocalDateTime.now());

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            user.setRefreshToken(refreshToken);
            userRepository.save(user);

            logger.info("User logged in successfully: {}", user.getPhoneNumber());

            return new AuthResponse(accessToken, refreshToken, user.getFullName(), user.getRole().name());

        } catch (BadCredentialsException e) {
            logger.warn("Failed login attempt for phone: {}", request.getPhoneNumber());
            throw new BadRequestException("Sai số điện thoại hoặc mật khẩu");
        }
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        logger.info("Token refresh attempt");

        if (!jwtService.validateToken(refreshToken)) {
            throw new BadRequestException("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        // QUAN TRỌNG: Verify token type
        String tokenType = jwtService.extractTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            logger.warn("Invalid token type for refresh: {}", tokenType);
            throw new BadRequestException("Invalid token type");
        }

        String phoneNumber = jwtService.extractPhoneNumber(refreshToken);
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy người dùng"));

        if (!refreshToken.equals(user.getRefreshToken())) {
            logger.warn("Refresh token mismatch for user: {}", phoneNumber);
            throw new BadRequestException("Refresh token không khớp");
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        logger.info("Token refreshed successfully for user: {}", user.getPhoneNumber());

        return new AuthResponse(newAccessToken, newRefreshToken, user.getFullName(), user.getRole().name());
    }
}