package com.TenX.Automobile.controller;

import com.TenX.Automobile.model.dto.request.ChatMessagePayload;
import com.TenX.Automobile.model.dto.request.SendMessageRequest;
import com.TenX.Automobile.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    @MessageMapping("/chat/{conversationId}")
    public void handleChatMessage(
            @DestinationVariable Long conversationId,
            @Valid @Payload ChatMessagePayload payload) {

        log.debug("Incoming websocket chat message for conversation {} from sender {}", conversationId, payload.getSenderId());
        SendMessageRequest request = new SendMessageRequest(conversationId, payload.getSenderId(), payload.getText());
        chatService.sendMessage(request);
    }

    @MessageExceptionHandler
    public void handleWebSocketException(Throwable exception) {
        log.error("Error processing websocket chat message", exception);
        // Optionally send error frames to clients here.
    }
}

