package com.TenX.Automobile.model.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Logout Request DTO
 * Can optionally include refresh token to revoke
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {

    private String refreshToken;
    
    @Builder.Default
    private boolean revokeAllTokens = false;
}
