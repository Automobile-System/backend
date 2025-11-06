package com.TenX.Automobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandMeterResponse {
    private Double demandPercentage;
    private String demandStatus; // e.g., "HIGH", "MEDIUM", "LOW"
    private Double bonusMultiplier;
}

