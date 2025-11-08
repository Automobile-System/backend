package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewResponse {
    private ActiveEmployees activeEmployees;
    private OngoingServices ongoingServices;
    private ProjectsPending projectsPending;
    private AvgCompletionTime avgCompletionTime;
    private List<SystemAlert> systemAlerts;
    private Map<String, String> taskDistribution;
    private Map<String, String> completionRateTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveEmployees {
        private Integer total;
        private Integer available;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OngoingServices {
        private Integer total;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectsPending {
        private Integer total;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvgCompletionTime {
        private Double value;
        private String unit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemAlert {
        private String message;
        private String employee;
        private String reason;
    }
}

