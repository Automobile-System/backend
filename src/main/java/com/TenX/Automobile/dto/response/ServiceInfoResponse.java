package com.TenX.Automobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for basic service information with booking statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInfoResponse {
    
    private Long serviceId;
    private String title;
    private String description;
    private String category;
    private Double cost;
    private Double estimatedHours;
    private String imageUrl;
    private Integer totalBookings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
