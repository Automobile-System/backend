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
public class ProjectDetailResponse {
    
    private Long projectId;
    private Long jobId;
    private String title;
    private String description;
    private String status; // Job status
    private String projectStatus; // Project status
    private LocalDateTime arrivingDate;
    private LocalDateTime completionDate;
    private BigDecimal cost;
    private Double estimatedHours;
    
    // Vehicle information
    private VehicleInfo vehicle;
    
    // Tasks information
    private List<TaskInfo> tasks;
    
    // Assigned employees with time logs
    private List<EmployeeInfo> assignedEmployees;
    
    private LocalDateTime bookedAt;
    private LocalDateTime updatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VehicleInfo {
        private String registrationNo;
        private String brandName;
        private String model;
        private Integer capacity;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskInfo {
        private Long taskId;
        private String taskTitle;
        private String taskDescription;
        private String status;
        private Double estimatedHours;
        private LocalDateTime completedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmployeeInfo {
        private String employeeId;
        private String firstName;
        private String lastName;
        private String specialty;
        private Double rating;
        private String profileImageUrl;
        private Double hoursWorked;
        private String workDescription;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}
