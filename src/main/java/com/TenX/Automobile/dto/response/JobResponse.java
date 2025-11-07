package com.TenX.Automobile.dto.response;

import com.TenX.Automobile.enums.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    
    private Long jobId;
    
    private JobType type;
    
    private Long typeId;
    
    private UUID vehicleId;
    
    private String vehicleRegistration;
    
    private String status;
    
    private LocalDateTime arrivingDate;
    
    private BigDecimal cost;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Details of the associated Service or Project
    private ServiceDetails serviceDetails;
    
    private ProjectDetails projectDetails;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceDetails {
        private Long serviceId;
        private String title;
        private String description;
        private String category;
        private String imageUrl;
        private Double estimatedHours;
        private Double serviceCost;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectDetails {
        private Long projectId;
        private String title;
        private String description;
        private Double estimatedHours;
        private Double projectCost;
        private String projectStatus;
        private Integer taskCount;
    }
}
