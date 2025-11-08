package com.TenX.Automobile.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ManageAssignJobRequest {
    
    @NotNull(message = "Job ID is required")
    private Long jobId;

    @NotNull(message = "Manager ID is required")
    private UUID managerId;

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;
}
