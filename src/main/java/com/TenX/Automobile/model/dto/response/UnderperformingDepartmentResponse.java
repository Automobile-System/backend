package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnderperformingDepartmentResponse {
    private String departmentName;
    private Double slowerCompletion;           // Percentage slower than benchmark
    private Integer avgCompletionTime;         // Average time in minutes
    private Integer targetTime;                // Target time in minutes
    private String rootCause;                  // AI-identified root cause
    private ManagerOversight managerOversight;
    private String recommendation;             // AI-generated action items

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManagerOversight {
        private String name;                   // Manager identifier
        private Integer score;                 // Oversight quality score (0-100)
        private Integer threshold;             // Minimum acceptable score
    }
}

