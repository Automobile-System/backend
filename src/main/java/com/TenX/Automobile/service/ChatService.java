package com.TenX.Automobile.service;

import com.TenX.Automobile.dto.request.SendMessageRequest;
import com.TenX.Automobile.dto.response.ConversationResponse;
import com.TenX.Automobile.dto.response.MessageResponse;
import com.TenX.Automobile.entity.*;
import com.TenX.Automobile.exception.ResourceNotFoundException;
import com.TenX.Automobile.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final BaseUserRepository userRepository;

    /**
     * Get all conversations for an employee
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversationsByEmployeeId(UUID employeeId) {
        log.info("Fetching conversations for employee ID: {}", employeeId);
        
        List<Conversation> conversations = conversationRepository.findByEmployeeId(employeeId);
        
        return conversations.stream()
                .map(this::convertToConversationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all messages for a conversation
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesByConversationId(Long conversationId) {
        log.info("Fetching messages for conversation ID: {}", conversationId);
        
        conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));
        
        List<Message> messages = messageRepository.findByConversationId(conversationId);
        
        return messages.stream()
                .map(this::convertToMessageResponse)
                .collect(Collectors.toList());
    }

    /**
     * Send a new message
     */
    public MessageResponse sendMessage(SendMessageRequest request) {
        log.info("Sending message to conversation ID: {}", request.getConversationId());
        
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + request.getConversationId()));
        
        UUID senderId = UUID.fromString(request.getSenderId());
        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found with id: " + senderId));
        
        // Validate sender is part of the conversation
        if (!sender.getId().equals(conversation.getEmployee().getId()) && 
            !sender.getId().equals(conversation.getParticipant().getId())) {
            throw new IllegalArgumentException("Sender is not part of this conversation");
        }
        
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .text(request.getText())
                .build();
        
        Message savedMessage = messageRepository.save(message);
        
        // Update conversation's updatedAt timestamp by saving it
        conversationRepository.save(conversation); // This triggers @LastModifiedDate
        
        log.info("Message sent successfully with ID: {}", savedMessage.getMessageId());
        
        return convertToMessageResponse(savedMessage);
    }

    /**
     * Convert Conversation entity to ConversationResponse DTO
     */
    private ConversationResponse convertToConversationResponse(Conversation conversation) {
        // Get recipient name (the participant, not the employee)
        String recipientName = null;
        if (conversation.getParticipant() != null) {
            String firstName = conversation.getParticipant().getFirstName();
            String lastName = conversation.getParticipant().getLastName();
            recipientName = (firstName != null ? firstName : "") + 
                          (lastName != null ? " " + lastName : "");
            recipientName = recipientName.trim();
            if (recipientName.isEmpty()) {
                recipientName = conversation.getParticipant().getEmail();
            }
        }
        
        // Get last message snippet
        String lastMessageSnippet = null;
        java.time.LocalDateTime lastMessageTime = null;
        
        List<Message> lastMessages = messageRepository.findLastMessagesByConversationId(conversation.getConversationId());
        if (lastMessages != null && !lastMessages.isEmpty()) {
            Message lastMessage = lastMessages.get(0); // First one is the most recent
            lastMessageSnippet = lastMessage.getText();
            // Truncate if too long
            if (lastMessageSnippet != null && lastMessageSnippet.length() > 50) {
                lastMessageSnippet = lastMessageSnippet.substring(0, 47) + "...";
            }
            lastMessageTime = lastMessage.getTimestamp();
        }
        
        // Get associated vehicle registration number
        String associatedVehicle = null;
        if (conversation.getVehicle() != null) {
            associatedVehicle = conversation.getVehicle().getRegistration_No();
        }
        
        return ConversationResponse.builder()
                .id(conversation.getConversationId())
                .recipientName(recipientName)
                .lastMessageSnippet(lastMessageSnippet)
                .lastMessageTime(lastMessageTime)
                .associatedVehicle(associatedVehicle)
                .build();
    }

    /**
     * Convert Message entity to MessageResponse DTO
     */
    private MessageResponse convertToMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getMessageId())
                .senderId(message.getSender() != null ? message.getSender().getId() : null)
                .text(message.getText())
                .timestamp(message.getTimestamp())
                .build();
    }
}
