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
public class SystemAlertResponse {
    private String id;
    private String type;  // 'warning' | 'info' | 'error'
    private String message;
    private LocalDateTime timestamp;  // ISO 8601 format
}

