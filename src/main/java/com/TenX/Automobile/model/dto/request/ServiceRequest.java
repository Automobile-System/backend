package com.TenX.Automobile.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotBlank(message = "Category is required")
    private String category;
    
    private String imageUrl;
    
    @NotNull(message = "Estimated hours is required")
    @Positive(message = "Estimated hours must be positive")
    private Double estimatedHours;
    
    @NotNull(message = "Cost is required")
    @Positive(message = "Cost must be positive")
    private Double cost;
}
