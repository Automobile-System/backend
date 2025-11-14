package com.TenX.Automobile.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProjectRequest {
    
    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;
    
    @NotBlank(message = "Project title is required")
    private String title;
    
    @NotBlank(message = "Project description is required")
    private String description;
}
