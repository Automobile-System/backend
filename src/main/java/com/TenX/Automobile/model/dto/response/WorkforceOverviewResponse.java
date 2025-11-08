package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkforceOverviewResponse {
    private WorkforceStats stats;
    private CenterInfo centerInfo;
    private OverloadedEmployee overloadedEmployee;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkforceStats {
        private Integer totalEmployees;
        private Integer activeEmployees;
        private Integer onLeave;
        private Integer frozen;
        private Double avgRating;
        private Double ratingChange;
        private Double avgWorkload;
        private String avgSalary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CenterInfo {
        private Integer totalCenters;
        private Integer activeManagers;
        private Integer minimumManagers;
        private Integer totalEmployees;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverloadedEmployee {
        private String name;
        private String specialization;
        private Integer capacityPercentage;
        private Integer activeTasks;
        private Integer maxTasks;
    }
}

