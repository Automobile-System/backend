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
public class CreateConversationRequest {

    /**
     * Participant initiating the chat (customer or manager) - UUID string.
     */
    @NotBlank(message = "Participant ID is required")
    private String participantId;

    /**
     * Preferred employee to assign. Optional; if omitted, the system will auto assign.
     */
    private String employeeId;

    /**
     * Optional vehicle to associate with the conversation.
     */
    private String vehicleId;
}

