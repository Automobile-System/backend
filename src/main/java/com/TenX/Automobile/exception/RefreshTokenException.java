package com.TenX.Automobile.exception;

/**
 * Exception thrown when refresh token is invalid or expired
 */
public class RefreshTokenException extends RuntimeException {
    
    public RefreshTokenException(String message) {
        super(message);
    }
    
    public RefreshTokenException(String token, String message) {
        super(String.format("Failed for [%s]: %s", token, message));
    }
}
