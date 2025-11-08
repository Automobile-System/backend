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
public class ReportsResponse {
    private Map<String, Integer> data;
    private List<DataPoint> dataList;
    private Double averageDelayDays;
    private String mostCommonReason;
    private String type;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        private String name;
        private Integer requests;
        private String month;
        private Integer delays;
        private String type;
        private Integer value;
    }
}

