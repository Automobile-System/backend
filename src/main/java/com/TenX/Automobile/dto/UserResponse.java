package com.TenX.Automobile.dto;

import com.TenX.Automobile.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for user response data (excluding sensitive information like password)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String phoneNumber;
    private String department;
    private String employeeId;
    private Long managerId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;

    // Computed fields
    private String fullName;
    private String roleDisplay;

    // Constructor with computed fields
    public UserResponse(Long id, String firstName, String lastName, String email, Role role,
                       String phoneNumber, String department, String employeeId, Long managerId,
                       Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt,
                       LocalDateTime lastLogin) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.department = department;
        this.employeeId = employeeId;
        this.managerId = managerId;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLogin = lastLogin;
        
        // Set computed fields
        this.fullName = firstName + " " + lastName;
        this.roleDisplay = role.name().substring(0, 1).toUpperCase() + 
                          role.name().substring(1).toLowerCase();
    }
}