package com.TenX.Automobile.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Min(value = 1, message = "Capacity must be at least 1")
    private int capacity;
}
