package com.TenX.Automobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialReportResponse {
    private List<ServiceTypeBreakdown> breakdown;
    private FinancialTotals totals;
    private Period period;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceTypeBreakdown {
        private String serviceType;
        private Double revenue;
        private Double cost;
        private Double profit;
        private Double margin;    // Percentage
        private Double trend;     // Percentage change (can be negative)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialTotals {
        private Double totalRevenue;
        private Double totalCost;
        private Double totalProfit;
        private Double overallMargin;    // Percentage
        private Double overallTrend;     // Percentage change
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Period {
        private String startDate;
        private String endDate;
    }
}

