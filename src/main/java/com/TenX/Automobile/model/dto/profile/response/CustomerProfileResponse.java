package com.TenX.Automobile.model.dto.profile.response;

import com.TenX.Automobile.model.enums.Role;
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
public class CustomerProfileResponse {
    private UUID id;
    private String customerId;
    private String email;
    private String firstName;
    private String lastName;
    private String nationalId;
    private String phoneNumber;
    private String profileImageUrl;
    private Set<Role> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}
