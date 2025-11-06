package com.TenX.Automobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {
    private UUID vehicleId;
    private String registrationNo;
    private String brandName;
    private String model;
    private int capacity;
    private String createdBy;
    private UUID customerId;
    private String customerEmail;
    private LocalDateTime createdAt;
}
