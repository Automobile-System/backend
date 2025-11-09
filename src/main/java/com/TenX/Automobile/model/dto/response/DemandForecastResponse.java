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
public class DemandForecastResponse {
    private Integer overallIncrease;           // Overall percentage increase
    private String forecastMonth;              // Month being forecasted
    private Integer projectedBookings;         // Total bookings projected
    private Integer changeFromPrevious;        // Numeric change from previous month
    private String previousMonth;              // Previous month name
    private List<ServiceBreakdown> serviceBreakdown;
    private HiringRecommendation hiringRecommendation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceBreakdown {
        private String serviceName;
        private Double percentageChange;  // Can be positive or negative
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HiringRecommendation {
        private Integer totalMechanics;
        private List<String> breakdown;  // Specific roles needed
    }
}

