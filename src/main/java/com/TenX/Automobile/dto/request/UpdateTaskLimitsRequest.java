package com.TenX.Automobile.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskLimitsRequest {
    @NotNull
    @Min(1)
    private Integer maxTasksPerDay;

    @NotNull
    @Min(1)
    private Integer overloadThreshold;
}

