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
public class ServiceAnalyticsDetailedResponse {
    private PopularServices popularServices;
    private AverageCost averageCost;
    private AverageDuration averageDuration;
    private CategoryPerformance categoryPerformance;
    private BrandAnalytics brandAnalytics;
    private JobTimeliness jobTimeliness;
    private TaskDelays taskDelays;
    private ProjectAnalytics projectAnalytics;
    private ServiceSummary serviceSummary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularServices {
        private List<String> labels;
        private List<Integer> data;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AverageCost {
        private List<String> labels;
        private List<Double> data;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AverageDuration {
        private List<String> labels;
        private List<Double> data;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryPerformance {
        private List<String> labels;
        private List<Integer> jobs;
        private List<Integer> delays;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandAnalytics {
        private List<String> labels;
        private List<Integer> data;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobTimeliness {
        private List<String> labels;
        private List<Integer> data;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskDelays {
        private Integer totalDelayed;
        private List<DelayBreakdown> breakdown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DelayBreakdown {
        private String employeeId;
        private String employeeName;
        private Integer delayedTasks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectAnalytics {
        private List<String> labels;
        private List<Double> estimated;
        private List<Double> actual;
        private ProjectSummary summary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectSummary {
        private Integer totalProjects;
        private Integer pending;
        private Integer approved;
        private Integer completed;
        private Integer inProgress;
        private Integer waitingParts;
        private Integer scheduled;
        private Integer cancelled;
        private Double averageCost;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceSummary {
        private Integer totalServices;
        private Integer completed;
        private Integer inProgress;
        private Integer waitingParts;
        private Integer scheduled;
        private Integer cancelled;
        private Double averageCost;
    }
}
