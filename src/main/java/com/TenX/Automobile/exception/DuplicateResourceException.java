package com.TenX.Automobile.exception;

/**
 * Exception thrown when trying to create a resource that already exists
 */
public class DuplicateResourceException extends RuntimeException {
    
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DuplicateResourceException(String resource, String field, String value) {
        super(resource + " already exists with " + field + ": " + value);
    }
}