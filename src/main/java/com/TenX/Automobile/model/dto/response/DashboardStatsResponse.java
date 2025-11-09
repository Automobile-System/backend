package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private ProfitThisMonth profitThisMonth;
    private ActiveCustomers activeCustomers;
    private OngoingServices ongoingServices;
    private ActiveEmployees activeEmployees;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfitThisMonth {
        private String value;        // Format: "LKR X.XM"
        private String change;        // Format: "X% from last month"
        private Integer percentage;   // Raw percentage value
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveCustomers {
        private Integer value;        // Total count
        private Integer newThisMonth; // New this month
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OngoingServices {
        private Integer value;        // Service count
        private String status;        // Status text
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveEmployees {
        private Integer value;        // Total active
        private Integer onLeave;      // On leave count
        private Integer frozen;       // Frozen/suspended count
    }
}

