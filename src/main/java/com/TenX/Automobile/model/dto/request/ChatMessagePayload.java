package com.TenX.Automobile.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessagePayload {

    @NotBlank(message = "Sender ID is required")
    private String senderId;

    @NotBlank(message = "Message text is required")
    private String text;
}
