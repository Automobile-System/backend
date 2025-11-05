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
    private List<String> vehicleRegistrations;
    private String message;
    private LocalDateTime bookedAt;
}
