package com.TenX.Automobile.dto.response;

import com.TenX.Automobile.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * WHAT: Standard response format for user data
 * WHY: Consistent API responses, hide sensitive data like password
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String nic;
    private String phoneNumber;
    private Set<Role> roles;
    private LocalDateTime createdAt;

}