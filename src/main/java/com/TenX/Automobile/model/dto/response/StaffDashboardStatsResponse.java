package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDashboardStatsResponse {
    private String employeeId;
    private String employeeName;
    private Integer tasksToday;
    private Integer completedTasks;
    private Double totalHours;
    private Double rating;
    private String currentMonthStart;
    private String currentMonthEnd;
}
