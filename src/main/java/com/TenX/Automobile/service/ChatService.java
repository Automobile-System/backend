package com.TenX.Automobile.service;

import com.TenX.Automobile.model.dto.request.CreateConversationRequest;
import com.TenX.Automobile.model.dto.request.SendMessageRequest;
import com.TenX.Automobile.model.dto.response.ConversationResponse;
import com.TenX.Automobile.model.dto.response.MessageResponse;
import com.TenX.Automobile.exception.ResourceNotFoundException;
import com.TenX.Automobile.model.entity.Conversation;
import com.TenX.Automobile.model.entity.Message;
import com.TenX.Automobile.model.entity.Employee;
import com.TenX.Automobile.model.entity.UserEntity;
import com.TenX.Automobile.model.entity.Vehicle;
import com.TenX.Automobile.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
    private final EmployeeRepository employeeRepository;
    private final VehicleRepository vehicleRepository;
    private final SimpMessagingTemplate messagingTemplate;

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
     * Get all conversations for a participant (customer/manager).
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversationsByParticipantId(UUID participantId) {
        log.info("Fetching conversations for participant ID: {}", participantId);

        List<Conversation> conversations = conversationRepository.findByParticipantId(participantId);

        return conversations.stream()
                .map(this::convertToConversationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create or return an existing conversation between an employee and
     * participant.
     */
    public ConversationResponse createConversation(CreateConversationRequest request) {
        log.info("Creating conversation. participantId={}, employeeId={}, vehicleId={}",
                request.getParticipantId(), request.getEmployeeId(), request.getVehicleId());

        UUID participantUuid = parseUuid(request.getParticipantId(), "Participant");
        UserEntity participant = userRepository.findById(participantUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found with id: " + participantUuid));

        Employee employee = resolveEmployee(request);

        Optional<Conversation> existingConversation = conversationRepository
                .findByEmployeeIdAndParticipantId(employee.getId(), participant.getId());

        Vehicle vehicle = resolveVehicle(request);
        Conversation conversation = existingConversation.map(conv -> {
            if (vehicle != null
                    && (conv.getVehicle() == null || !vehicle.getV_Id().equals(conv.getVehicle().getV_Id()))) {
                conv.setVehicle(vehicle);
                conversationRepository.save(conv);
            }
            return conv;
        })
                .orElseGet(() -> {
                    Conversation newConversation = Conversation.builder()
                            .employee(employee)
                            .participant(participant)
                            .vehicle(vehicle)
                            .build();
                    return conversationRepository.save(newConversation);
                });

        return convertToConversationResponse(conversation);
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
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found with id: " + request.getConversationId()));

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

        MessageResponse response = convertToMessageResponse(savedMessage);
        try {
            messagingTemplate.convertAndSend("/topic/chat/" + conversation.getConversationId(), response);
        } catch (Exception e) {
            log.warn("Failed to publish websocket message for conversation {}: {}", conversation.getConversationId(),
                    e.getMessage());
        }

        return response;
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

        List<Message> lastMessages = messageRepository
                .findLastMessagesByConversationId(conversation.getConversationId());
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

        String participantId = conversation.getParticipant() != null
                ? conversation.getParticipant().getId().toString()
                : null;

        String employeeId = conversation.getEmployee() != null
                ? conversation.getEmployee().getId().toString()
                : null;

        String employeeName = null;
        if (conversation.getEmployee() != null) {
            String firstName = conversation.getEmployee().getFirstName();
            String lastName = conversation.getEmployee().getLastName();
            employeeName = (firstName != null ? firstName : "") +
                    (lastName != null ? " " + lastName : "");
            employeeName = employeeName.trim();
            if (employeeName.isEmpty()) {
                employeeName = conversation.getEmployee().getEmail();
            }
        }

        return ConversationResponse.builder()
                .id(conversation.getConversationId())
                .recipientName(recipientName)
                .lastMessageSnippet(lastMessageSnippet)
                .lastMessageTime(lastMessageTime)
                .associatedVehicle(associatedVehicle)
                .participantId(participantId)
                .employeeId(employeeId)
                .employeeName(employeeName)
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

    private Employee resolveEmployee(CreateConversationRequest request) {
        if (request.getEmployeeId() != null && !request.getEmployeeId().isBlank()) {
            UUID employeeUuid = parseUuid(request.getEmployeeId(), "Employee");
            return employeeRepository.findById(employeeUuid)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeUuid));
        }

        return employeeRepository.findEnabledStaffEmployees().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active employees available to take the conversation"));
    }

    private Vehicle resolveVehicle(CreateConversationRequest request) {
        if (request.getVehicleId() == null || request.getVehicleId().isBlank()) {
            return null;
        }
        UUID vehicleUuid = parseUuid(request.getVehicleId(), "Vehicle");
        return vehicleRepository.findById(vehicleUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleUuid));
    }

    private UUID parseUuid(String value, String label) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(label + " ID must be a valid UUID");
        }
    }
}
