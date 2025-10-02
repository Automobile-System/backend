package com.TenX.Automobile.exception;

/**
 * Exception thrown when an invalid role is encountered
 */
public class InvalidRoleException extends RuntimeException {
    
    public InvalidRoleException(String message) {
        super(message);
    }
    
    public InvalidRoleException(String message, Throwable cause) {
        super(message, cause);
    }
}