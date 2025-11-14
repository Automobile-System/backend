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
public class CustomerAnalyticsResponse {
    private CustomerStats stats;
    private List<CustomerDetail> topCustomers;
    private CustomerGrowthTrend growthTrend;
    private VehicleBrandDistribution vehicleBrandDistribution;
    private CustomerEngagement engagement;
    private RevenueByCustomer revenueStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerStats {
        private Integer totalCustomers;
        private Integer activeCustomers;
        private Integer newThisMonth;
        private Integer inactiveCustomers;
        private Double retentionRate;
        private Integer totalVehicles;
        private Integer totalJobsCompleted;
        private Double totalRevenue;
        private Double avgSpendPerCustomer;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerDetail {
        private String customerId;
        private String name;
        private String email;
        private String phone;
        private String joinDate;
        private Integer vehicleCount;
        private Integer totalJobs;
        private Integer completedJobs;
        private Integer activeJobs;
        private Double totalSpent;
        private String lastServiceDate;
        private List<String> vehicleBrands;
        private String status;
        private Double engagementScore; // Based on service frequency
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerGrowthTrend {
        private List<String> labels; // ["Jan 2025", "Feb 2025", ...]
        private List<Integer> newCustomers; // New customers per month
        private List<Integer> totalCustomers; // Cumulative total
        private List<Integer> activeCustomers; // Active per month
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleBrandDistribution {
        private List<String> brands; // ["Toyota", "Honda", ...]
        private List<Integer> count; // Number of vehicles per brand
        private List<Integer> customerCount; // Number of customers per brand
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerEngagement {
        private Integer highEngagement; // 5+ services
        private Integer mediumEngagement; // 2-4 services
        private Integer lowEngagement; // 1 service
        private Integer noEngagement; // 0 services
        private Double avgServicesPerCustomer;
        private List<EngagementByMonth> monthlyEngagement;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EngagementByMonth {
        private String month;
        private Integer activeCustomers;
        private Integer totalServices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueByCustomer {
        private Double totalRevenue;
        private Double avgRevenuePerCustomer;
        private List<TopSpender> topSpenders;
        private List<MonthlyRevenue> monthlyRevenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopSpender {
        private String customerId;
        private String name;
        private Double totalSpent;
        private Integer servicesCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRevenue {
        private String month;
        private Double revenue;
        private Integer customerCount;
    }
}
