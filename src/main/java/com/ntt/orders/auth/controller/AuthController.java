package com.ntt.orders.auth.controller;

import com.ntt.orders.auth.dto.request.LoginRequest;
import com.ntt.orders.auth.dto.request.RefreshTokenRequest;
import com.ntt.orders.auth.dto.request.RegisterRequest;
import com.ntt.orders.auth.dto.response.AuthResponse;
import com.ntt.orders.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication and authorization")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register new user",
            description = "Create a new customer account with phone number and password",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Registration successful"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or phone number already exists")
            }
    )
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(
            summary = "User login",
            description = "Authenticate user with phone number and password",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "400", description = "Invalid credentials")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
            summary = "Refresh access token",
            description = "Get new access token using refresh token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid or expired refresh token")
            }
    )
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @Operation(
            summary = "User logout",
            description = "Invalidate user session and clear refresh token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Logout successful"),
                    @ApiResponse(responseCode = "401", description = "User not authenticated")
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Gọi service logout nếu có xử lý server-side
        return ResponseEntity.ok().build();
    }
}