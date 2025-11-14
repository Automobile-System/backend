package com.TenX.Automobile.controller;

import com.TenX.Automobile.model.dto.request.SendMessageRequest;
import com.TenX.Automobile.model.dto.response.ConversationResponse;
import com.TenX.Automobile.model.dto.response.MessageResponse;
import com.TenX.Automobile.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Chat Controller - Employee chat and communication endpoints
 * Handles messaging between employees, customers, and managers
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class ChatController {

    private final ChatService chatService;

    /**
     * GET /api/chat/conversations/{employeeId}
     * Load Chat List
     * Retrieves a list of active conversation threads (Customer & Manager)
     */
    @GetMapping("/conversations/{employeeId}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ConversationResponse>> getConversations(@PathVariable UUID employeeId) {
        log.info("Fetching conversations for employee ID: {}", employeeId);
        List<ConversationResponse> conversations = chatService.getConversationsByEmployeeId(employeeId);
        return ResponseEntity.ok(conversations);
    }

    /**
     * GET /api/chat/messages/{conversationId}
     * Load Message History
     * Retrieves the full history for a selected conversation
     */
    @GetMapping("/messages/{conversationId}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable Long conversationId) {
        log.info("Fetching messages for conversation ID: {}", conversationId);
        List<MessageResponse> messages = chatService.getMessagesByConversationId(conversationId);
        return ResponseEntity.ok(messages);
    }

    /**
     * POST /api/chat/messages
     * Send New Message
     * Submits a message to a specific conversation
     */
    @PostMapping("/messages")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        log.info("Sending message to conversation ID: {}", request.getConversationId());
        try {
            MessageResponse message = chatService.sendMessage(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid message request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

