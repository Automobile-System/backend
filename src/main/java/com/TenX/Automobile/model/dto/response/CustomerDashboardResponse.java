package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDashboardResponse {
    private Long activeServices;
    private Long completedServices;
    private Long upcomingAppointments;
    private Long activeProjects;
    private Long completedProjects;
}
