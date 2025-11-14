package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleServiceHistoryResponse {
    
    // Vehicle information
    private String vehicleRegistration;
    private String vehicleBrand;
    private String vehicleModel;
    private Integer vehicleCapacity;
    
    // Job statistics
    private Integer totalJobs;
    private Integer completedJobs;
    private Integer activeJobs;
    
    // List of all jobs (services and projects)
    private List<JobHistoryDetail> jobHistory;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobHistoryDetail {
        // Job basic info
        private Long jobId;
        private String jobType; // SERVICE or PROJECT
        private String jobStatus;
        private LocalDateTime arrivingDate;
        private LocalDateTime completionDate;
        private BigDecimal cost;
        
        // Service/Project details
        private Long typeId;
        private String title;
        private String description;
        private Double estimatedHours;
        
        // For services only
        private String category;
        private String imageUrl;
        
        // For projects only
        private String projectStatus;
        private Integer totalTasks;
        private Integer completedTasks;
        
        // Assigned employees
        private List<AssignedEmployeeDetail> assignedEmployees;
        
        // Timestamps
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignedEmployeeDetail {
        private String employeeId;
        private String employeeName;
        private String specialty;
        private Double rating;
        private String profileImageUrl;
        private Double hoursWorked;
        private String workDescription;
        private LocalDateTime assignedAt;
    }
}
