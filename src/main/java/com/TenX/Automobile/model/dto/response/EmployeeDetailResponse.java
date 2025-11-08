package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetailResponse {
    private String id;
    private String name;
    private String specialization;
    private String email;
    private String phone;
    private Double rating;
    private String status;  // 'Active' | 'On Leave' | 'Frozen'
}

