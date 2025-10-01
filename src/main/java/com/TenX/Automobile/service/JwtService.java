package com.TenX.Automobile.service;

import com.TenX.Automobile.entity.User;
import com.TenX.Automobile.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Enterprise-level JWT Service with role-based token management
 */
@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:3600000}") // Default 1 hour in milliseconds
    private long defaultExpirationTime;

    @Value("${jwt.extended-expiration:86400000}") // Default 24 hours for "remember me"
    private long extendedExpirationTime;

    private SecretKey getSigningKey() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters long");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a JWT token for a user with role information
     */
    public String generateToken(User user) {
        return generateToken(user, false);
    }

    /**
     * Generate a JWT token with optional extended expiration
     */
    public String generateToken(User user, boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());
        
        if (user.getEmployeeId() != null) {
            claims.put("employeeId", user.getEmployeeId());
        }
        if (user.getDepartment() != null) {
            claims.put("department", user.getDepartment());
        }

        long expirationTime = rememberMe ? extendedExpirationTime : defaultExpirationTime;
        return createToken(claims, user.getEmail(), expirationTime);
    }

    /**
     * Generate token for email (backward compatibility)
     */
    public String generateToken(String email) {
        log.warn("Generating token with email only - consider using generateToken(User) for better security");
        return createToken(new HashMap<>(), email, defaultExpirationTime);
    }

    /**
     * Create JWT token with claims and expiration
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationTime) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract email (subject) from token
     */
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extract user ID from token
     */
    public Long extractUserId(String token) {
        Claims claims = getClaims(token);
        Object userId = claims.get("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    /**
     * Extract role from token
     */
    public Role extractRole(String token) {
        Claims claims = getClaims(token);
        String role = claims.get("role", String.class);
        return role != null ? Role.valueOf(role) : null;
    }

    /**
     * Extract employee ID from token
     */
    public String extractEmployeeId(String token) {
        return getClaims(token).get("employeeId", String.class);
    }

    /**
     * Extract department from token
     */
    public String extractDepartment(String token) {
        return getClaims(token).get("department", String.class);
    }

    /**
     * Extract first name from token
     */
    public String extractFirstName(String token) {
        return getClaims(token).get("firstName", String.class);
    }

    /**
     * Extract last name from token
     */
    public String extractLastName(String token) {
        return getClaims(token).get("lastName", String.class);
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return getClaims(token).getExpiration();
    }

    /**
     * Get token expiration time in seconds
     */
    public Long getExpirationInSeconds() {
        return defaultExpirationTime / 1000;
    }

    /**
     * Get extended token expiration time in seconds
     */
    public Long getExtendedExpirationInSeconds() {
        return extendedExpirationTime / 1000;
    }

    /**
     * Validate token
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = getClaims(token);
            return !isTokenExpired(claims);
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate token for specific user
     */
    public boolean isTokenValid(String token, User user) {
        try {
            String tokenEmail = extractEmail(token);
            return tokenEmail.equals(user.getEmail()) && !isTokenExpired(token);
        } catch (Exception e) {
            log.debug("Token validation failed for user {}: {}", user.getEmail(), e.getMessage());
            return false;
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        return isTokenExpired(getClaims(token));
    }

    /**
     * Check if token is expired using claims
     */
    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    /**
     * Check if user has required role based on token
     */
    public boolean hasRequiredRole(String token, Role requiredRole) {
        try {
            Role userRole = extractRole(token);
            return userRole != null && userRole.hasAuthorityOver(requiredRole);
        } catch (Exception e) {
            log.debug("Role validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get all claims from token
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract token from Authorization header
     */
    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
