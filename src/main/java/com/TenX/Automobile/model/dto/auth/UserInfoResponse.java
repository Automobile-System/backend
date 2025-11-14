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
 * User Info DTO
 * Returns current authenticated user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoResponse {

    private String userId;
    
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    private String phoneNumber;
    
    private String nationalId;
    
    private String profileImageUrl;
    
    private Set<Role> roles;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastLoginAt;
    
    private LocalDateTime lockedUntil;
    
    private boolean enabled;
    
    private boolean accountNonExpired;
    
    private boolean accountNonLocked;
    
    private boolean credentialsNonExpired;
    
    private int failedLoginAttempts;
}
