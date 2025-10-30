package com.TenX.Automobile.dto.auth;

import com.TenX.Automobile.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Login Response DTO
 * Contains authentication tokens and user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private String accessToken;
    
    private String refreshToken;
    
    @Builder.Default
    private String tokenType = "Bearer";
    
    private Long expiresIn;
    
    private String userId;
    
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    private Set<Role> roles;
    
    private LocalDateTime lastLoginAt;
    
    private boolean rememberMe;
    
    private String message;

    /**
     * Create success response
     */
    public static LoginResponse success(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            String userId,
            String email,
            String firstName,
            String lastName,
            Set<Role> roles,
            LocalDateTime lastLoginAt,
            boolean rememberMe) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .userId(userId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .roles(roles)
                .lastLoginAt(lastLoginAt)
                .rememberMe(rememberMe)
                .message("Login successful")
                .build();
    }
}
