package com.TenX.Automobile.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialReportRequest {
    @NotBlank
    private String serviceFilter;  // 'all' | 'predefined' | 'custom'
    
    @NotBlank
    private String startDate;     // Format: YYYY-MM-DD
    
    @NotBlank
    private String endDate;       // Format: YYYY-MM-DD
}

