package com.TenX.Automobile.dto.response;

import com.TenX.Automobile.enums.NotificationType;
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
public class NotificationResponse {
    private Long notificationId;
    private String message;
    private NotificationType type;
    private Boolean isRead;
    private UUID userId;
    private String userName;
    private String userEmail;
    private Long jobId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
