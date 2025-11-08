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
public class ProfitProjectionResponse {
    private Double monthOverMonthGrowth;       // Average monthly growth percentage
    private List<MonthlyProfit> trajectory;    // 3-month profit trajectory
    private YearEndTarget yearEndTarget;
    private String optimizationTip;            // AI-generated recommendation

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyProfit {
        private String month;
        private String profit;  // Formatted string (e.g., "1.7M")
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearEndTarget {
        private String monthlyProfit;  // Target monthly profit
        private String date;            // Target achievement date
        private Double annualGrowth;    // Annual growth percentage
    }
}

