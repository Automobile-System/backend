package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class CompletionRatePercentageResponse {
    private String chartType="line";
    private String title="Completion Rate Percentage Over Time";
    private DataPoint[] data;

    @Data
    @Builder
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class DataPoint {
        private String month;
        private Double rate; //completionRatePercentage
        private Integer completedTasks; //Total completed jobs(services or projects)
        private Integer totalTasks; //Total jobs(services or projects)

    }
}
