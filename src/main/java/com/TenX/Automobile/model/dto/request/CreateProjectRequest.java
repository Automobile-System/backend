package com.TenX.Automobile.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {
    @NotBlank
    private String customerName;
    
    @NotBlank
    private String contactNumber;
    
    @NotBlank
    private String vehicleRegistration;
    
    @NotBlank
    private String vehicleModel;
    
    @NotBlank
    private String projectTitle;
    
    @NotBlank
    private String projectDescription;
    
    @NotNull
    private LocalDate startDate;
    
    @NotNull
    private LocalDate estimatedCompletionTime;
    
    @NotNull
    private BigDecimal totalProjectCost;
    
    private List<SubTaskRequest> subTasks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubTaskRequest {
        @NotBlank
        private String name;
        
        @NotNull
        private Double hours;
    }
}

