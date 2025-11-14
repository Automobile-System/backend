package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerListResponse {
    private String id;  // customerId
    private String name;  // firstName + lastName
    private String email;
    private String phone;
    private Integer vehicleCount;
    private Double totalSpent;
    private String lastServiceDate;  // Format: YYYY-MM-DD or "N/A"
    private String status;  // "Active" | "Inactive"
}

