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
public class EmployeeProfileResponse {
    private UUID id;
    private String name; // firstName + lastName
    private String firstName;
    private String lastName;
    private Set<Role> role; // roles
    private String email;
    private String phone; // phoneNumber
    private LocalDateTime joinDate; // createdAt
    private Double currentRating; // TODO: May need to be calculated from reviews if available
    private Integer totalReviews; // TODO: May need to be calculated from reviews if available
    private String specialty;
    private String employeeId;
    private String profileImageUrl;
}

