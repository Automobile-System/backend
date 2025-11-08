package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillShortagePredictionResponse {
    private String skillArea;                  // Skill category at risk
    private Double demandIncrease;             // Demand increase percentage
    private Integer availableEmployees;       // Current workforce count
    private Integer crisisWeeks;               // Weeks until crisis point
    private ImpactForecast impactForecast;
    private List<String> actionPlan;           // Ordered action items

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImpactForecast {
        private String delayedCustomers;      // Expected delayed customers
        private String month;                  // Crisis month
        private Double revenueLoss;            // Projected revenue loss
    }
}

