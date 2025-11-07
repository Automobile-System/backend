package com.TenX.Automobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeListResponse {
    private String id;
    private String name;
    private String skill;
    private String currentTasks;
    private Double rating;
    private String status;
}

