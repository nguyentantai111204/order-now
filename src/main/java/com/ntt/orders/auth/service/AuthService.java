package com.ntt.orders.auth.service;

import com.ntt.orders.auth.dto.request.LoginRequest;
import com.ntt.orders.auth.dto.request.RegisterRequest;
import com.ntt.orders.auth.dto.response.AuthResponse;
import com.ntt.orders.auth.mapper.AuthMapper;
import com.ntt.orders.shared.common.response.ApiResponse;
import com.ntt.orders.shared.common.response.ResponseCode;
import com.ntt.orders.user.entity.User;
import com.ntt.orders.user.repository.UserRepository;
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
    private final AuthMapper authMapper;

    @Transactional
    public ApiResponse<AuthResponse> register(RegisterRequest request) {
        logger.info("Registration attempt for phone: {}", request.getPhoneNumber());

        try {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                return ApiResponse.error(
                        "Số điện thoại đã tồn tại!",
                        ResponseCode.DUPLICATE_ENTRY
                );
            }

            // Validate password strength
            if (request.getPassword().length() < 6) {
                return ApiResponse.error(
                        "Mật khẩu phải có ít nhất 6 ký tự",
                        ResponseCode.VALIDATION_ERROR
                );
            }

            User user = authMapper.toEntity(request);
            // ✅ QUAN TRỌNG: Encode password trước khi save
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            userRepository.save(user);

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            user.setRefreshToken(refreshToken);
            userRepository.save(user);

            logger.info("User registered successfully: {}", user.getPhoneNumber());

            AuthResponse authResponse = authMapper.toAuthResponse(user, accessToken, refreshToken);
            return ApiResponse.created(authResponse);

        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage());
            return ApiResponse.error(
                    "Đăng ký thất bại: " + e.getMessage(),
                    ResponseCode.INTERNAL_ERROR
            );
        }
    }

    @Transactional
    public ApiResponse<AuthResponse> login(LoginRequest request) {
        logger.info("Login attempt for phone: {}", request.getPhoneNumber());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getPhoneNumber().trim(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByPhoneNumber(request.getPhoneNumber().trim())
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            user.setLastLoginAt(java.time.LocalDateTime.now());

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            user.setRefreshToken(refreshToken);
            userRepository.save(user);

            logger.info("User logged in successfully: {}", user.getPhoneNumber());

            AuthResponse authResponse = authMapper.toAuthResponse(user, accessToken, refreshToken);
            return ApiResponse.success("Đăng nhập thành công", authResponse);

        } catch (BadCredentialsException e) {
            logger.warn("Failed login attempt for phone: {}", request.getPhoneNumber());
            return ApiResponse.error(
                    "Sai số điện thoại hoặc mật khẩu",
                    ResponseCode.INVALID_CREDENTIALS
            );
        } catch (Exception e) {
            logger.error("Login failed: {}", e.getMessage());
            return ApiResponse.error(
                    "Đăng nhập thất bại: " + e.getMessage(),
                    ResponseCode.INTERNAL_ERROR
            );
        }
    }

    @Transactional
    public ApiResponse<Void> logout(String refreshToken) {
        try {
            String phoneNumber = jwtService.extractPhoneNumber(refreshToken);
            User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
            if (user != null) {
                user.setRefreshToken(null);
                userRepository.save(user);
                logger.info("User logged out successfully: {}", phoneNumber);
                return ApiResponse.success("Đăng xuất thành công", null);
            }
            return ApiResponse.error("Người dùng không tồn tại", ResponseCode.USER_NOT_FOUND);
        } catch (Exception e) {
            logger.warn("Logout failed: {}", e.getMessage());
            return ApiResponse.success("Đăng xuất thành công", null);
        }
    }

    @Transactional
    public ApiResponse<AuthResponse> refreshToken(String refreshToken) {
        logger.info("Token refresh attempt");

        try {
            if (!jwtService.validateToken(refreshToken)) {
                return ApiResponse.error(
                        "Refresh token không hợp lệ hoặc đã hết hạn",
                        ResponseCode.UNAUTHORIZED
                );
            }

            String tokenType = jwtService.extractTokenType(refreshToken);
            if (!"refresh".equals(tokenType)) {
                logger.warn("Invalid token type for refresh: {}", tokenType);
                return ApiResponse.error(
                        "Loại token không hợp lệ",
                        ResponseCode.UNAUTHORIZED
                );
            }

            String phoneNumber = jwtService.extractPhoneNumber(refreshToken);
            User user = userRepository.findByPhoneNumber(phoneNumber)
                    .orElse(null);

            if (user == null) {
                return ApiResponse.error(
                        "Không tìm thấy người dùng",
                        ResponseCode.USER_NOT_FOUND
                );
            }

            if (!refreshToken.equals(user.getRefreshToken())) {
                // Token đã được sử dụng → thu hồi tất cả tokens của user
                user.setRefreshToken(null);
                userRepository.save(user);
                return ApiResponse.error(
                        "Refresh token đã được sử dụng. Vui lòng đăng nhập lại.",
                        ResponseCode.UNAUTHORIZED
                );
            }

            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            user.setRefreshToken(newRefreshToken);
            userRepository.save(user);

            logger.info("Token refreshed successfully for user: {}", user.getPhoneNumber());

            AuthResponse authResponse = authMapper.toAuthResponse(user, newAccessToken, newRefreshToken);
            return ApiResponse.success("Làm mới token thành công", authResponse);

        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            return ApiResponse.error(
                    "Làm mới token thất bại: " + e.getMessage(),
                    ResponseCode.INTERNAL_ERROR
            );
        }
    }
}