package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectBoardResponse {
    private String status;
    private List<ProjectSummary> projects;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectSummary {
        private String id;
        private Long projectId;
        private String title;
        private String description;
        private Double estimatedHours;
        private Double cost;
        private String customer;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private CustomerInfo customerDetails;
        private VehicleInfo vehicle;
        private JobInfo job;
        private List<TaskSummary> tasks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        private UUID id;
        private String customerId;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleInfo {
        private UUID vehicleId;
        private String registrationNumber;
        private String brandName;
        private String model;
        private Integer capacity;
        private String createdBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobInfo {
        private Long jobId;
        private String type;
        private Long typeId;
        private String status;
        private LocalDateTime arrivingDate;
        private LocalDateTime completionDate;
        private BigDecimal cost;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskSummary {
        private Long taskId;
        private String title;
        private String description;
        private String status;
        private Double estimatedHours;
        private LocalDateTime completedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}