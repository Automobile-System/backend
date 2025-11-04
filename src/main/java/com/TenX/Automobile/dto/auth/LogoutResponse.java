package com.TenX.Automobile.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Logout Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogoutResponse {

    private String message;
    
    private boolean success;

    /**
     * Create success response
     */
    public static LogoutResponse success() {
        return LogoutResponse.builder()
                .message("Logout successful")
                .success(true)
                .build();
    }
}
