package com.TenX.Automobile.controller;

import com.TenX.Automobile.model.dto.request.CreateConversationRequest;
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
     * GET /api/chat/conversations/participant/{participantId}
     * Fetch conversations for the participant (e.g., customer)
     */
    @GetMapping("/conversations/participant/{participantId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER', 'ADMIN', 'STAFF')")
    public ResponseEntity<List<ConversationResponse>> getParticipantConversations(@PathVariable UUID participantId) {
        log.info("Fetching conversations for participant ID: {}", participantId);
        List<ConversationResponse> conversations = chatService.getConversationsByParticipantId(participantId);
        return ResponseEntity.ok(conversations);
    }

    /**
     * POST /api/chat/conversations
     * Create or reuse conversation between participant and employee.
     */
    @PostMapping("/conversations")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<ConversationResponse> createConversation(
            @Valid @RequestBody CreateConversationRequest request) {
        log.info("Creating conversation between participant {} and employee {}", request.getParticipantId(), request.getEmployeeId());
        ConversationResponse conversation = chatService.createConversation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
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

