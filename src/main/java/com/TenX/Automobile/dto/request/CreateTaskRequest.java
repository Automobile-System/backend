package com.TenX.Automobile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    @NotBlank
    private String customerName;
    
    @NotBlank
    private String contactNumber;
    
    @NotBlank
    private String vehicleRegistration;
    
    @NotBlank
    private String vehicleModel;
    
    @NotBlank
    private String serviceType;
    
    private String serviceNotes;
    
    @NotNull
    private Double estimatedDurationHours;
    
    @NotNull
    private BigDecimal estimatedPrice;
    
    private LocalDate preferredDate;
    
    private LocalTime preferredTime;
    
    private UUID assignedEmployeeId;
}

