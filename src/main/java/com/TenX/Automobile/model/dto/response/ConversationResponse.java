package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private Long id;
    private String recipientName;
    private String lastMessageSnippet;
    private LocalDateTime lastMessageTime;
    private String associatedVehicle; // Vehicle registration number
}

