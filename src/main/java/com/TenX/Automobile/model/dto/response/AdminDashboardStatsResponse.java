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
public class AdminDashboardStatsResponse {
    private DashboardKPIs kpis;
    private MonthlyProfitTrend profitTrend;
    private JobProjectCompletion jobProjectCompletion;
    private ServiceCategoryDistribution serviceCategoryDistribution;
    private List<TopEmployeeByHours> topEmployees;
    private List<BusinessAlert> alerts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardKPIs {
        private Integer totalCustomers;
        private Integer totalEmployees;
        private Integer totalManagers;
        private Integer ongoingJobs;
        private Integer ongoingProjects;
        private Double monthlyRevenue;
        private Integer completedServices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyProfitTrend {
        private List<String> labels;
        private List<Double> revenue;
        private List<Double> cost;
        private List<Double> profit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobProjectCompletion {
        private StatusCounts jobs;
        private StatusCounts projects;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusCounts {
        private Integer completed;
        private Integer in_progress;
        private Integer on_hold;
        private Integer pending;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceCategoryDistribution {
        private List<String> labels;
        private List<Integer> data;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopEmployeeByHours {
        private Long employeeId;
        private String name;
        private String specialty;
        private Integer totalHours;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessAlert {
        private Long id;
        private String type;
        private String message;
        private String severity;
        private String createdAt;
        private Boolean isRead;
        private Long relatedId;
    }
}
