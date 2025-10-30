package com.TenX.Automobile.exception;

/**
 * Exception thrown when account is locked due to too many failed login attempts
 */
public class AccountLockedException extends RuntimeException {
    
    private final String email;
    private final long lockDurationMinutes;
    
    public AccountLockedException(String email, long lockDurationMinutes) {
        super(String.format("Account locked for email: %s. Please try again after %d minutes.", 
                email, lockDurationMinutes));
        this.email = email;
        this.lockDurationMinutes = lockDurationMinutes;
    }
    
    public String getEmail() {
        return email;
    }
    
    public long getLockDurationMinutes() {
        return lockDurationMinutes;
    }
}
