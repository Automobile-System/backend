package com.TenX.Automobile.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateEmployeeProfileRequest {
    
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 10, message = "Phone number must be 10 digits local number")
    @Pattern(
        regexp = "^(\\+94\\d{9}|0\\d{9})$",
        message = "Phone number must be local (07XXXXXXXX) or international (+947XXXXXXXX)"
    )
    private String phoneNumber;

    private String profileImageUrl;
}

