package com.TenX.Automobile.dto.response;

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
public class ServiceDetailResponse {
    
    // Service Information
    private Long serviceId;
    private String title;
    private String description;
    private String category;
    private String status;
    private LocalDateTime arrivingDate;
    private BigDecimal cost;
    private Double estimatedHours;
    private String imageUrl;
    
    // Vehicle Information
    private VehicleInfo vehicle;
    
    // Tasks Information
    private List<TaskInfo> tasks;
    
    // Assigned Employees
    private List<EmployeeInfo> assignedEmployees;
    
    // Timestamps
    private LocalDateTime bookedAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleInfo {
        private String registrationNo;
        private String brandName;
        private String model;
        private Integer capacity;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskInfo {
        private Long taskId;
        private String taskTitle;
        private String taskDescription;
        private String status;
        private Double estimatedHours;
        private LocalDateTime completedAt;
        private LocalDateTime createdAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeInfo {
        private String employeeId;
        private String firstName;
        private String lastName;
        private String specialty;
        private Double hoursWorked;
        private String workDescription;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}
