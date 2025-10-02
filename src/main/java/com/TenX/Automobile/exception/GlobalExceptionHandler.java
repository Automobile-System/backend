package com.TenX.Automobile.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for enterprise-level error management
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        Map<String, Object> errorResponse = createErrorResponse(
                "Validation failed",
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST,
                request.getDescription(false)
        );
        errorResponse.put("fieldErrors", fieldErrors);

        log.warn("Validation error: {}", fieldErrors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle illegal argument exceptions (business logic errors)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = createErrorResponse(
                ex.getMessage(),
                "BUSINESS_LOGIC_ERROR",
                HttpStatus.BAD_REQUEST,
                request.getDescription(false)
        );

        log.warn("Business logic error: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = createErrorResponse(
                "Authentication failed",
                "AUTHENTICATION_ERROR",
                HttpStatus.UNAUTHORIZED,
                request.getDescription(false)
        );

        log.warn("Authentication error: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle bad credentials specifically
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = createErrorResponse(
                "Invalid email or password",
                "INVALID_CREDENTIALS",
                HttpStatus.UNAUTHORIZED,
                request.getDescription(false)
        );

        log.warn("Invalid credentials attempt from: {}", request.getRemoteUser());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = createErrorResponse(
                "Access denied. Insufficient permissions.",
                "ACCESS_DENIED",
                HttpStatus.FORBIDDEN,
                request.getDescription(false)
        );

        log.warn("Access denied for user: {} on path: {}", request.getRemoteUser(), request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle user not found exceptions
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = createErrorResponse(
                ex.getMessage(),
                "USER_NOT_FOUND",
                HttpStatus.NOT_FOUND,
                request.getDescription(false)
        );

        log.warn("User not found: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle duplicate resource exceptions
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResourceException(
            DuplicateResourceException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = createErrorResponse(
                ex.getMessage(),
                "DUPLICATE_RESOURCE",
                HttpStatus.CONFLICT,
                request.getDescription(false)
        );

        log.warn("Duplicate resource error: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle role validation exceptions
     */
    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRoleException(
            InvalidRoleException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = createErrorResponse(
                ex.getMessage(),
                "INVALID_ROLE",
                HttpStatus.BAD_REQUEST,
                request.getDescription(false)
        );

        log.warn("Invalid role error: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        
        Map<String, Object> errorResponse = createErrorResponse(
                "An unexpected error occurred. Please try again later.",
                "INTERNAL_SERVER_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request.getDescription(false)
        );

        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Create standardized error response
     */
    private Map<String, Object> createErrorResponse(String message, String errorCode, 
                                                    HttpStatus status, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("path", path.replace("uri=", ""));
        return errorResponse;
    }
}