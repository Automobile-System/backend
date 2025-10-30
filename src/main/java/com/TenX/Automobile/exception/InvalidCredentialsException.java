package com.TenX.Automobile.exception;

/**
 * Exception thrown when user credentials are invalid
 */
public class InvalidCredentialsException extends RuntimeException {
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
