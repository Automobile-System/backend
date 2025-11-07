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

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            jakarta.servlet.http.HttpServletResponse httpResponse) {

        log.info("Login request received for email: {}", request.getEmail());
        LoginResult result = authService.login(request, httpRequest);
        LoginTokens tokens = result.getTokens();
        LoginResponse loginResponse = result.getResponse();

        // Set JWT access token in HttpOnly cookie
    Cookie jwtCookie = new Cookie("accessToken", tokens.getAccessToken());
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true); // Set to true in production (requires HTTPS)
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(tokens.getExpiresIn() != null ? (int)(tokens.getExpiresIn() / 1000) : 15 * 60); // default 15 min
        httpResponse.addCookie(jwtCookie);

        // Optionally, you may remove the accessToken from the response body for extra security
        // response.setAccessToken(null);

        return ResponseEntity.ok(loginResponse);
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        
        log.debug("Token refresh request received");
        RefreshTokenResponse response = authService.refreshToken(request, httpRequest);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LogoutResponse> logout(@RequestBody(required = false) LogoutRequest request, HttpServletResponse httpResponse) {
        log.info("Logout request received");
        
        if (request == null) {
            request = LogoutRequest.builder().build();
        }
        
        LogoutResponse response = authService.logout(request);

        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0); // Immediately expire
        httpResponse.addCookie(accessTokenCookie);

        // Clear refresh token cookie
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // Immediately expire
        httpResponse.addCookie(refreshTokenCookie);

        // Clear remember me token cookie (if used)
        Cookie rememberMeCookie = new Cookie("rememberMe", null);
        rememberMeCookie.setHttpOnly(true);
        rememberMeCookie.setSecure(true);
        rememberMeCookie.setPath("/");
        rememberMeCookie.setMaxAge(0);
        httpResponse.addCookie(rememberMeCookie);

        log.info("All authentication cookies cleared for user logout");
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
    public ResponseEntity<?> getCurrentUser(org.springframework.security.core.Authentication authentication) {
        log.info("Get current user request - Authentication: {}, Authorities: {}", 
                authentication != null ? authentication.getName() : "null",
                authentication != null ? authentication.getAuthorities() : "null");
        
        UserInfoResponse response = authService.getCurrentUser();
        log.info("User info response - Roles: {}", response.getRoles());
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
