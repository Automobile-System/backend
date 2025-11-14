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
public class ServiceListResponse {
    private Long serviceId;
    private String title;
    private String description;
    private String category;
    private String status;
    private LocalDateTime arrivingDate;
    private BigDecimal cost;
    private Double estimatedHours;
    private String vehicleRegistration;
    private String vehicleBrand;
    private String vehicleModel;
    private List<AssignedEmployeeSummary> assignedEmployees;
    private LocalDateTime bookedAt;
    
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
