package com.TenX.Automobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private Integer tasksToday;
    private Integer tasksCompletedThisMonth;
    private Double totalHoursLoggedThisMonth;
    private Double averageRating; // TODO: Implement rating calculation if available
}

