package com.TenX.Automobile.service;

import com.TenX.Automobile.dto.auth.*;
import com.TenX.Automobile.entity.RefreshToken;
import com.TenX.Automobile.entity.UserEntity;
import com.TenX.Automobile.exception.AccountLockedException;
import com.TenX.Automobile.exception.InvalidCredentialsException;
import com.TenX.Automobile.exception.RefreshTokenException;
import com.TenX.Automobile.repository.BaseUserRepository;
import com.TenX.Automobile.security.constants.SecurityConstants;
import com.TenX.Automobile.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Enhanced Authentication Service with enterprise-level security features
 * - JWT token generation and validation
 * - Refresh token rotation
 * - Remember-me functionality
 * - Account lockout mechanism
 * - Login attempt tracking
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final BaseUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;

    /**
     * Authenticate user with email and password
     * Implements account lockout and failed attempt tracking
     */
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail().toLowerCase().trim();
        
        log.info("Login attempt for email: {}", email);

        // Check if account is locked
        if (loginAttemptService.isAccountLocked(email)) {
            log.warn("Login attempt for locked account: {}", email);
            throw new AccountLockedException(email, SecurityConstants.ACCOUNT_LOCK_DURATION_MINUTES);
        }

        // Check if lock duration has passed and unlock account
        loginAttemptService.checkAndUnlockAccount(email);

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            UserEntity user = (UserEntity) authentication.getPrincipal();

            // Check if user is disabled
            if (!user.isEnabled()) {
                log.warn("Login attempt for disabled account: {}", email);
                loginAttemptService.recordFailedLogin(email, "Account disabled", httpRequest);
                throw new DisabledException("Account has been disabled");
            }

            // Generate tokens
            String accessToken = jwtTokenProvider.generateAccessToken(
                    user.getId().toString(),
                    user.getEmail(),
                    user.getRoles(),
                    request.isRememberMe()
            );

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                    user,
                    request.isRememberMe(),
                    httpRequest
            );

            // Update user last login
            user.setLastLoginAt(LocalDateTime.now());
            user.setLastLoginIp(getClientIpAddress(httpRequest));
            userRepository.save(user);

            // Record successful login
            loginAttemptService.recordSuccessfulLogin(email, httpRequest);

            log.info("Successful login for user: {}", email);

            return LoginResponse.success(
                    accessToken,
                    refreshToken.getToken(),
                    jwtTokenProvider.getAccessTokenValidity(),
                    user.getId().toString(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRoles(),
                    user.getLastLoginAt(),
                    request.isRememberMe()
            );

        } catch (BadCredentialsException ex) {
            log.warn("Failed login attempt for email: {} - Invalid credentials", email);
            loginAttemptService.recordFailedLogin(email, "Invalid credentials", httpRequest);
            throw new InvalidCredentialsException("Invalid email or password");
        } catch (DisabledException ex) {
            log.warn("Failed login attempt for email: {} - Account disabled", email);
            loginAttemptService.recordFailedLogin(email, "Account disabled", httpRequest);
            throw ex;
        } catch (Exception ex) {
            log.error("Login error for email: {}", email, ex);
            loginAttemptService.recordFailedLogin(email, ex.getMessage(), httpRequest);
            throw new InvalidCredentialsException("Authentication failed");
        }
    }

    /**
     * Refresh access token using refresh token
     * Implements token rotation for enhanced security
     */
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String requestRefreshToken = request.getRefreshToken();

        log.debug("Refresh token request received");

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshToken -> {
                    // Verify token expiration
                    if (!refreshTokenService.verifyExpiration(refreshToken)) {
                        refreshTokenService.revokeToken(requestRefreshToken);
                        throw new RefreshTokenException(requestRefreshToken, "Refresh token expired");
                    }

                    UserEntity user = refreshToken.getUser();

                    // Rotate refresh token (invalidate old, create new)
                    RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(
                            refreshToken,
                            httpRequest
                    );

                    // Generate new access token
                    String newAccessToken = jwtTokenProvider.generateAccessToken(
                            user.getId().toString(),
                            user.getEmail(),
                            user.getRoles(),
                            refreshToken.isRememberMe()
                    );

                    log.info("Token refreshed successfully for user: {}", user.getEmail());

                    return RefreshTokenResponse.success(
                            newAccessToken,
                            newRefreshToken.getToken(),
                            jwtTokenProvider.getAccessTokenValidity()
                    );
                })
                .orElseThrow(() -> {
                    log.warn("Invalid refresh token: {}", requestRefreshToken);
                    return new RefreshTokenException(requestRefreshToken, "Invalid refresh token");
                });
    }

    /**
     * Logout user and revoke tokens
     */
    @Transactional
    public LogoutResponse logout(LogoutRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getPrincipal() instanceof UserEntity) {
                UserEntity user = (UserEntity) authentication.getPrincipal();
                
                if (request.isRevokeAllTokens()) {
                    // Revoke all refresh tokens for this user
                    refreshTokenService.revokeAllUserTokens(user);
                    log.info("Revoked all tokens for user: {}", user.getEmail());
                } else if (request.getRefreshToken() != null) {
                    // Revoke specific refresh token
                    refreshTokenService.revokeToken(request.getRefreshToken());
                    log.info("Revoked specific token for user: {}", user.getEmail());
                }
            }

            // Clear security context
            SecurityContextHolder.clearContext();

            return LogoutResponse.success();
            
        } catch (Exception ex) {
            log.error("Error during logout", ex);
            throw new RuntimeException("Logout failed");
        }
    }

    /**
     * Get current authenticated user information
     */
    @Transactional(readOnly = true)
    public UserInfoResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof UserEntity)) {
            throw new InvalidCredentialsException("User not authenticated");
        }

        UserEntity user = (UserEntity) authentication.getPrincipal();

        return UserInfoResponse.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .enabled(user.isEnabled())
                .accountNonExpired(user.isAccountNonExpired())
                .accountNonLocked(user.isAccountNonLocked())
                .credentialsNonExpired(user.isCredentialsNonExpired())
                .build();
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}