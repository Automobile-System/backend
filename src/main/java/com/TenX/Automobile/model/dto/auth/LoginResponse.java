package com.TenX.Automobile.model.dto.auth;

import com.TenX.Automobile.model.enums.Role;
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
    
    private String userId;
    
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    private Set<Role> roles;

    private Long expiresIn;
    
    private LocalDateTime lastLoginAt;
    
    private boolean rememberMe;
    
    private String message;


}
