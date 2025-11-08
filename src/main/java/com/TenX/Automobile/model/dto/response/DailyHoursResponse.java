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
public class DailyHoursResponse {
    private String weekStart;
    private String weekEnd;
    private String chartType;
    private List<String> labels;
    private List<Double> data;
    private Double totalHours;
    private Double averageHoursPerDay;
}
