package com.TenX.Automobile.service;

import com.TenX.Automobile.entity.RefreshToken;
import com.TenX.Automobile.entity.UserEntity;
import com.TenX.Automobile.repository.RefreshTokenRepository;
import com.TenX.Automobile.security.config.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing Refresh Tokens
 * Handles token creation, validation, rotation, and cleanup
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    /**
     * Create a new refresh token for user
     * Supports remember-me functionality with extended expiration
     */
    @Transactional
    public RefreshToken createRefreshToken(UserEntity user, boolean rememberMe, HttpServletRequest request) {
        // Revoke existing tokens for this user (one token per user policy)
        revokeAllUserTokens(user);

        long validity = rememberMe 
            ? jwtProperties.getRememberMeTokenValidity() 
            : jwtProperties.getRefreshTokenValidity();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(validity))
                .revoked(false)
                .rememberMe(rememberMe)
                .ipAddress(getClientIpAddress(request))
                .userAgent(getUserAgent(request))
                .build();

        refreshToken = refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user: {}, remember-me: {}", user.getEmail(), rememberMe);
        
        return refreshToken;
    }

    /**
     * Validate and retrieve refresh token
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(RefreshToken::isValid);
    }

    /**
     * Verify if refresh token is valid
     */
    @Transactional(readOnly = true)
    public boolean verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            log.warn("Refresh token expired for user: {}", token.getUser().getEmail());
            return false;
        }
        return true;
    }

    /**
     * Rotate refresh token (invalidate old, create new)
     * Best practice for security - prevents token reuse
     */
    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken, HttpServletRequest request) {
        // Revoke the old token
        oldToken.revoke();
        refreshTokenRepository.save(oldToken);

        // Create new token
        RefreshToken newToken = createRefreshToken(
            oldToken.getUser(), 
            oldToken.isRememberMe(), 
            request
        );

        log.info("Rotated refresh token for user: {}", oldToken.getUser().getEmail());
        return newToken;
    }

    /**
     * Revoke specific refresh token
     */
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshToken.revoke();
                    refreshTokenRepository.save(refreshToken);
                    log.info("Revoked refresh token for user: {}", refreshToken.getUser().getEmail());
                });
    }

    /**
     * Revoke all refresh tokens for a user
     */
    @Transactional
    public void revokeAllUserTokens(UserEntity user) {
        refreshTokenRepository.revokeAllUserTokens(user);
        log.info("Revoked all refresh tokens for user: {}", user.getEmail());
    }

    /**
     * Delete all tokens for a user (used when deleting user account)
     */
    @Transactional
    public void deleteAllUserTokens(UserEntity user) {
        refreshTokenRepository.deleteByUser(user);
        log.info("Deleted all refresh tokens for user: {}", user.getEmail());
    }

    /**
     * Scheduled task to clean up expired and revoked tokens
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired and revoked refresh tokens");
        
        // Delete expired tokens
        refreshTokenRepository.deleteAllExpiredTokens();
        
        // Delete revoked tokens older than 30 days
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        refreshTokenRepository.deleteRevokedTokensOlderThan(thirtyDaysAgo);
        
        log.info("Completed cleanup of expired and revoked refresh tokens");
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

    /**
     * Extract user agent from request
     */
    private String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.length() > 512) {
            return userAgent.substring(0, 512);
        }
        return userAgent;
    }
}
