package com.TenX.Automobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsServicesResponse {
    private String serviceId;
    private String serviceName;
    private Double basePrice;
    private String duration;  // Format: "45 mins" or "2 hours"
    private String requiredSkill;
    private String status;     // 'Active' | 'Inactive'
}

