package com.TenX.Automobile.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * JWT Configuration Properties
 * Externalizes JWT configuration for easy management across environments
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {

    /**
     * Secret key for signing JWT tokens
     * Must be at least 256 bits (32 characters) for HS256 algorithm
     */
    @NotBlank(message = "JWT secret key is required")
    private String secretKey;

    /**
     * Access token expiration time in milliseconds
     * Default: 15 minutes (900000ms)
     */
    @Min(value = 60000, message = "Access token validity must be at least 1 minute")
    private Long accessTokenValidity = 900000L;

    /**
     * Refresh token expiration time in milliseconds
     * Default: 7 days (604800000ms)
     */
    @Min(value = 3600000, message = "Refresh token validity must be at least 1 hour")
    private Long refreshTokenValidity = 604800000L;

    /**
     * Remember-me token expiration time in milliseconds
     * Default: 30 days (2592000000ms)
     */
    @Min(value = 86400000, message = "Remember-me token validity must be at least 1 day")
    private Long rememberMeTokenValidity = 2592000000L;

    /**
     * Token issuer identifier
     */
    @NotBlank(message = "Token issuer is required")
    private String issuer = "automobile-enterprise-system";

    /**
     * Token audience identifier
     */
    @NotBlank(message = "Token audience is required")
    private String audience = "automobile-web-app";
}
