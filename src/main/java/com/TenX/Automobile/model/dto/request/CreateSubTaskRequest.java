package com.TenX.Automobile.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubTaskRequest {

    @NotNull
    private Long projectId;

    @NotBlank
    private String title;

    private String description;

    private Double estimatedHours;

    private String status;
}

