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
public class ServicesAnalyticsResponse {
    private Summary summary;
    private List<ServicePerformance> servicePerformance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private MostProfitableService mostProfitableService;
        private Integer totalServicesMonth;
        private Integer changeFromLastMonth;
        private Integer partsReplaced;
        private Integer partsUsageRate;
        private Integer customerRetention;
        private Integer retentionImprovement;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MostProfitableService {
        private String name;
        private Double profit;
        private Double margin;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServicePerformance {
        private String id;
        private String name;
        private Integer totalBookings;
        private String avgDuration;  // Human-readable: "45 min" or "2 hours"
        private Double profitPerService;
        private Double customerRating;
        private Double trend;  // Percentage change
    }
}

