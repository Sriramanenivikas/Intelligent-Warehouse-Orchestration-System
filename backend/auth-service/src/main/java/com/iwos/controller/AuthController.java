package com.iwos.controller;

import com.iwos.dto.*;
import com.iwos.entity.RefreshToken;
import com.iwos.entity.Role;
import com.iwos.entity.User;
import com.iwos.service.RefreshTokenService;
import com.iwos.service.UserService;
import com.iwos.util.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AuthController
 * Handles authentication and authorization endpoints
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Register new user
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register new user: {}", request.getUsername());

        // Register user
        User user = userService.registerUser(request);

        // Generate tokens
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), roles);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Convert user to UserInfo
        UserInfo userInfo = userService.toUserInfo(user);

        // Build response
        LoginResponse response = LoginResponse.of(
                accessToken,
                refreshToken.getToken(),
                jwtTokenProvider.getAccessTokenExpirationInSeconds(),
                userInfo
        );

        log.info("User registered successfully: {}", user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * User login
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("User login: {}", request.getUsername());

        // Authenticate user
        User user = userService.authenticateUser(request.getUsername(), request.getPassword());

        // Generate tokens
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), roles);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Update last login
        userService.updateLastLogin(user.getUsername());

        // Convert user to UserInfo
        UserInfo userInfo = userService.toUserInfo(user);

        // Build response
        LoginResponse response = LoginResponse.of(
                accessToken,
                refreshToken.getToken(),
                jwtTokenProvider.getAccessTokenExpirationInSeconds(),
                userInfo
        );

        log.info("User logged in successfully: {}", user.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh JWT token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh JWT token");

        // Verify refresh token
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());

        // Get user
        User user = refreshToken.getUser();

        // Generate new access token
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), roles);

        // Convert user to UserInfo
        UserInfo userInfo = userService.toUserInfo(user);

        // Build response (use same refresh token)
        LoginResponse response = LoginResponse.of(
                accessToken,
                refreshToken.getToken(),
                jwtTokenProvider.getAccessTokenExpirationInSeconds(),
                userInfo
        );

        log.info("Access token refreshed successfully for user: {}", user.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get authenticated user information")
    public ResponseEntity<UserInfo> getCurrentUser() {
        log.info("Get current user");

        // Get authenticated user from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Get user
        User user = userService.getUserByUsername(username);

        // Convert to UserInfo
        UserInfo userInfo = userService.toUserInfo(user);

        return ResponseEntity.ok(userInfo);
    }

    /**
     * Logout user
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Revoke refresh token and logout user")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Logout user");

        // Revoke refresh token
        refreshTokenService.revokeToken(request.getRefreshToken());

        log.info("User logged out successfully");
        return ResponseEntity.ok(MessageResponse.of("Logged out successfully"));
    }

    /**
     * Health check endpoint for auth service
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if auth service is running")
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(MessageResponse.of("Auth service is running"));
    }

}
