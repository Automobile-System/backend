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
public class ChartsDataResponse {
    private List<DailyHoursData> dailyHoursData;
    private List<MonthlyTasksData> monthlyTasksData;
    private List<RatingTrendData> ratingTrendData;
    private List<ServiceDistributionData> serviceDistributionData;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyHoursData {
        private String date;
        private Double hours;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTasksData {
        private String month;
        private Integer completedTasks;
        private Integer totalTasks;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingTrendData {
        private String date;
        private Double rating;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceDistributionData {
        private String serviceType;
        private Integer count;
    }
}

