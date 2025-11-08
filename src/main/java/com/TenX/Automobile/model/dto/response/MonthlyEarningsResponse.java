package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyEarningsResponse {
    private Double baseSalary;
    private Double performanceBonus;
    private Double demandBonus;
    private Double overtimePay;
    private Double totalEarnings;
    private Double overtimeHours;
}

