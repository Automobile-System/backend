package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManageAssignJobResponse {
    private UUID assignmentId;
    private Long jobId;
    private UUID managerId;
    private String managerName;
    private String managerEmail;
    private UUID employeeId;
    private String employeeName;
    private String employeeEmail;
    private LocalDateTime createdAt;
}
