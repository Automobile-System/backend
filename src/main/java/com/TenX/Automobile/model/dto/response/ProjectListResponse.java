package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectListResponse {
    
    private Long projectId;
    private Long jobId;
    private String title;
    private String description;
    private String status; // Job status: PENDING, IN_PROGRESS, COMPLETED
    private String projectStatus; // Project status
    private LocalDateTime arrivingDate;
    private LocalDateTime completionDate;
    private BigDecimal cost;
    private Double estimatedHours;
    
    // Vehicle information
    private String vehicleRegistration;
    private String vehicleBrand;
    private String vehicleModel;
    
    // Assigned employees summary
    private List<AssignedEmployeeSummary> assignedEmployees;
    private Integer totalTasks;
    private Integer completedTasks;
    
    private LocalDateTime bookedAt;
    private LocalDateTime updatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignedEmployeeSummary {
        private String employeeId;
        private String firstName;
        private String lastName;
        private String specialty;
        private String profileImageUrl;
    }
}
