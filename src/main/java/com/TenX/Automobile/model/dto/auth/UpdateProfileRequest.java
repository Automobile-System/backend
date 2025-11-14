package com.TenX.Automobile.model.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;
    
    private String nationalId;
    
    private String profileImageUrl;
    
    // Password change fields
    private String currentPassword;
    private String newPassword;
}
