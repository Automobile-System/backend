package com.TenX.Automobile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VehicleRequest {
    
    @NotBlank(message = "Registration number is required")
    @Size(max = 20, message = "Registration number must not exceed 20 characters")
    private String registrationNo;

    @NotBlank(message = "Brand name is required")
    @Size(max = 50, message = "Brand name must not exceed 50 characters")
    private String brandName;

    @NotBlank(message = "Model is required")
    @Size(max = 50, message = "Model must not exceed 50 characters")
    private String model;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be greater than 0")
    private Integer capacity;
}
