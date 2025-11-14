package com.TenX.Automobile.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
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
public class CreateServiceRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    private String description;

    @Size(max = 50)
    private String category;

    @Size(max = 255)
    private String imageUrl;

    @DecimalMin(value = "0.0", inclusive = false, message = "Estimated hours must be positive")
    private Double estimatedHours;

    @DecimalMin(value = "0.0", inclusive = false, message = "Cost must be positive")
    private Double cost;
}

