package com.TenX.Automobile.security.jwt;

import com.TenX.Automobile.enums.Role;
import com.TenX.Automobile.security.config.JwtProperties;
import com.TenX.Automobile.security.constants.SecurityConstants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT Token Provider - Enterprise-level token management
 * Handles token generation, validation, and extraction
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    protected void init() {
        // Initialize the secret key from properties
        byte[] keyBytes = jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT Token Provider initialized successfully");
    }

    /**
     * Generate access token with user details and roles
     */
    public String generateAccessToken(String userId, String email, Set<Role> roles, boolean rememberMe) {
        Date now = new Date();
        long validity = rememberMe 
            ? jwtProperties.getRememberMeTokenValidity() 
            : jwtProperties.getAccessTokenValidity();
        Date expiryDate = new Date(now.getTime() + validity);

        Map<String, Object> claims = new HashMap<>();
        claims.put(SecurityConstants.CLAIM_KEY_USER_ID, userId);
        claims.put(SecurityConstants.CLAIM_KEY_EMAIL, email);
        claims.put(SecurityConstants.CLAIM_KEY_ROLES, roles.stream()
                .map(Role::name)
                .collect(Collectors.toSet()));
        claims.put(SecurityConstants.CLAIM_KEY_CREATED, now);
        claims.put(SecurityConstants.CLAIM_KEY_REMEMBER_ME, rememberMe);

        return Jwts.builder()
                .claims(claims)
                .subject(userId)
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAudience()).and()
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Generate access token from Authentication object
     */
    public String generateAccessToken(Authentication authentication, boolean rememberMe) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Set<Role> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.replace("ROLE_", ""))
                .map(Role::valueOf)
                .collect(Collectors.toSet());

        return generateAccessToken(
                userDetails.getUsername(),
                userDetails.getUsername(),
                roles,
                rememberMe
        );
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshTokenValidity());

        return Jwts.builder()
                .subject(userId)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Extract user ID from JWT token
     */
    public String getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * Extract email from JWT token
     */
    public String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get(SecurityConstants.CLAIM_KEY_EMAIL, String.class);
    }

    /**
     * Extract roles from JWT token
     */
    @SuppressWarnings("unchecked")
    public Set<String> getRolesFromToken(String token) {
        Claims claims = parseToken(token);
        return (Set<String>) claims.get(SecurityConstants.CLAIM_KEY_ROLES);
    }

    /**
     * Check if token has remember-me flag
     */
    public Boolean isRememberMeToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get(SecurityConstants.CLAIM_KEY_REMEMBER_ME, Boolean.class);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get token expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Parse JWT token and extract claims
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get token validity in milliseconds
     */
    public Long getAccessTokenValidity() {
        return jwtProperties.getAccessTokenValidity();
    }

    /**
     * Get refresh token validity in milliseconds
     */
    public Long getRefreshTokenValidity() {
        return jwtProperties.getRefreshTokenValidity();
    }
}
