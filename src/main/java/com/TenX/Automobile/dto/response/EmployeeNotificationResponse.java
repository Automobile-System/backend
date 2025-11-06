package com.TenX.Automobile.dto.response;

import com.TenX.Automobile.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeNotificationResponse {
    private Long id;
    private String message;
    private NotificationType type;
    private LocalDateTime timestamp;
}

