package com.TenX.Automobile.controller;

import com.TenX.Automobile.dto.auth.*;
import com.TenX.Automobile.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller - Enterprise-level authentication endpoints
 * Handles login, logout, token refresh, and user information retrieval
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Login endpoint
     * Authenticates user with email and password
     * 
     * @param request Login request with email, password, and rememberMe flag
     * @param httpRequest HTTP servlet request for IP tracking
     * @return LoginResponse with JWT tokens and user information
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            jakarta.servlet.http.HttpServletResponse httpResponse) {

        log.info("Login request received for email: {}", request.getEmail());
        LoginResponse response = authService.login(request, httpRequest);

        // Set JWT access token in HttpOnly cookie
    Cookie jwtCookie = new Cookie("accessToken", response.getAccessToken());
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true); // Set to true in production (requires HTTPS)
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(response.getExpiresIn() != null ? (int)(response.getExpiresIn() / 1000) : 15 * 60); // default 15 min
        httpResponse.addCookie(jwtCookie);

        // Optionally, you may remove the accessToken from the response body for extra security
        // response.setAccessToken(null);

        return ResponseEntity.ok(response);
    }

    /**
     * Refresh token endpoint
     * Generates new access token using refresh token
     * Implements token rotation for enhanced security
     * 
     * @param request Refresh token request
     * @param httpRequest HTTP servlet request for tracking
     * @return RefreshTokenResponse with new access and refresh tokens
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        
        log.debug("Token refresh request received");
        RefreshTokenResponse response = authService.refreshToken(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout endpoint
     * Revokes refresh tokens and clears authentication
     * 
     * @param request Logout request (optional refresh token)
     * @return LogoutResponse with success message
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LogoutResponse> logout(@RequestBody(required = false) LogoutRequest request) {
        log.info("Logout request received");
        
        if (request == null) {
            request = LogoutRequest.builder().build();
        }
        
        LogoutResponse response = authService.logout(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user information
     * Returns authenticated user details
     * 
     * @return UserInfoResponse with current user information
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserInfoResponse> getCurrentUser() {
        log.debug("Get current user request received");
        UserInfoResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint for authentication service
     * 
     * @return Success message
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Authentication service is running");
    }
}
