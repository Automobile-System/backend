package com.TenX.Automobile.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectUpdateRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @Positive(message = "Estimated hours must be positive")
    private Double estimatedHours;
    
    @Positive(message = "Cost must be positive")
    private Double cost;
    
    private String status; // Project status: PLANNING, IN_PROGRESS, COMPLETED, ON_HOLD
}
