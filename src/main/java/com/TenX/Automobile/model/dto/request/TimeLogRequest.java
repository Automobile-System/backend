package com.TenX.Automobile.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TimeLogRequest {
    
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Positive(message = "Hours worked must be greater than 0")
    private Double hoursWorked;

    private String description;
}
