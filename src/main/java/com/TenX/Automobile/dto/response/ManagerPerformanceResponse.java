package com.TenX.Automobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerPerformanceResponse {
    private String id;
    private String name;
    private Integer tasksAssigned;
    private Double completionRate;
    private Double avgEmployeeRating;
    private String status;  // 'Active' | 'Under Review'
}

