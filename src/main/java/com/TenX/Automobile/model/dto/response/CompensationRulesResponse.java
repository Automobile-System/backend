package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompensationRulesResponse {
    private Double baseSalary;
    private Double demandBonusPercentage;
    private Double exampleBonus;      // Auto-calculated
    private Double exampleTotal;       // Auto-calculated
}

