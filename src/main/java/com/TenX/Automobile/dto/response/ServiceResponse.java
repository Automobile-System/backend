package com.TenX.Automobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {
    
    private Long serviceId;
    private String title;
    private String description;
    private String category;
    private String imageUrl;
    private Double estimatedHours;
    private Double cost;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
