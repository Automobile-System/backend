package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignedTaskResponse {
    private Long id;
    private String title;
    private String status;
    private LocalDateTime deadline;
    private Double timeSpent;
    private UUID vehicleId;
    private String vehicleRegNo;
    private String customerName;
    private List<String> teamMembers; // Optional, if applicable
}
