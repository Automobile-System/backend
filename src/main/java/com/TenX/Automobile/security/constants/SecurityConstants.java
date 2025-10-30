package com.TenX.Automobile.security.constants;

/**
 * Security-related constants for JWT authentication and authorization
 * Enterprise-level security configuration constants
 */
public final class SecurityConstants {

    // JWT Token Constants
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE = "JWT";
    public static final String TOKEN_ISSUER = "automobile-enterprise-system";
    public static final String TOKEN_AUDIENCE = "automobile-web-app";

    // JWT Claims
    public static final String CLAIM_KEY_USER_ID = "userId";
    public static final String CLAIM_KEY_EMAIL = "email";
    public static final String CLAIM_KEY_ROLES = "roles";
    public static final String CLAIM_KEY_AUTHORITIES = "authorities";
    public static final String CLAIM_KEY_CREATED = "created";
    public static final String CLAIM_KEY_REMEMBER_ME = "rememberMe";

    // Security URLs
    public static final String[] PUBLIC_URLS = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/actuator/health",
            "/actuator/info",
            "/error",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    // Role-based URL patterns
    public static final String ADMIN_URL_PATTERN = "/api/v1/admin/**";
    public static final String MANAGER_URL_PATTERN = "/api/v1/manager/**";
    public static final String STAFF_URL_PATTERN = "/api/v1/staff/**";
    public static final String CUSTOMER_URL_PATTERN = "/api/v1/customer/**";

    // Security Configuration
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final long ACCOUNT_LOCK_DURATION_MINUTES = 30;
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 100;

    // Token Expiration Times (in milliseconds)
    public static final long ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000; // 15 minutes
    public static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 days
    public static final long REMEMBER_ME_TOKEN_VALIDITY = 30L * 24 * 60 * 60 * 1000; // 30 days

    // Rate Limiting
    public static final int MAX_REQUESTS_PER_MINUTE = 60;

    private SecurityConstants() {
        // Private constructor to prevent instantiation
        throw new IllegalStateException("Constants class");
    }
}
