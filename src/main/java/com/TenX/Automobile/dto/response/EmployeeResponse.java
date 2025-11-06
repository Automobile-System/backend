package com.TenX.Automobile.dto.response;

import com.TenX.Automobile.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private UUID id;
    private String employeeId;
    private String email;
    private String firstName;
    private String lastName;
    private String nationalId;
    private String phoneNumber;
    private String profileImageUrl;
    private String specialty;
    private Set<Role> roles;
    private LocalDateTime joinedDate;
    private LocalDateTime lastLoginAt;
    private boolean enabled;
}
