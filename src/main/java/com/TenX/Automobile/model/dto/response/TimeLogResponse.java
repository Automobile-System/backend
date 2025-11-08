package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogResponse {
    private Long id;
    private LocalDate date;
    private String taskTitle;
    private String vehicleRegNo;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double durationHours;
    private String remarks; // description field from TimeLog
}