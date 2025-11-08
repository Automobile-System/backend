package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerResponse {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String joinDate;  // Formatted: "Jan 15, 2023"
    private String status;     // 'Active' | 'Under Review' | 'Frozen'
}

