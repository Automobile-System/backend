package com.TenX.Automobile.model.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class EmployeeDetailsForCustomer {
    private UUID id;
    private String fullName;
    private LocalDateTime createdAt;
    private String specialty;
    private Double empRating;
    private String profileImage;  
}
