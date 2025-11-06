package com.TenX.Automobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceBookingResponse {

    private Long jobId;
    private Long serviceId;
    private String title;
    private String description;
    private String status;
    private LocalDateTime arrivingDate;
    private BigDecimal cost;
    private Double estimatedHours;
    private String category;
    private String vehicleRegistration;
    private String message;
    private LocalDateTime bookedAt;
}
