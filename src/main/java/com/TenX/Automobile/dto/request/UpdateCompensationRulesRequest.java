package com.TenX.Automobile.dto.request;

import jakarta.validation.constraints.Max;
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
public class UpdateCompensationRulesRequest {
    @NotNull
    @Min(1)
    private Double baseSalary;

    @NotNull
    @Min(0)
    @Max(100)
    private Double demandBonusPercentage;
}

