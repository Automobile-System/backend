package com.TenX.Automobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInsightResponse {
    private String id;
    private String title;           // Max 60 chars recommended
    private String description;     // Max 200 chars recommended
    private String category;        // 'forecast' | 'projection' | 'warning' | 'recommendation'
    private String icon;            // Icon name (optional)
}

