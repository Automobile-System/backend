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
public class TimeLogResponse {
    private Long logId;
    private Double hoursWorked;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long jobId;
    private Boolean isActive;
    private Boolean isCompleted;
}
