package com.TenX.Automobile.dto.request;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;


/**
 * WHAT: Base DTO with common registration fields for all user types
 * WHY: Avoid code duplication, ensure consistent validation
 * ALTERNATIVES: Separate DTOs for each user type (more code duplication)
 */
@Data
public class BaseUserRegistrationRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;


    @NotBlank(message = "NIC is required")
    @Pattern(
            regexp = "^([0-9]{12}|[0-9]{8}[Vv])$",
            message = "NIC must be 12 digits or 8 digits followed by V/v"
    )
    private String nationalId;

    @NotBlank(message = "Phone number is required")
    @Size(max=10,message="Phone number must be 10 digits local number")
    @Pattern(
            regexp = "^(\\+94\\d{9}|0\\d{9})$",
            message = "Phone number must be local (07XXXXXXXX) or international (+947XXXXXXXX)"
    )
    private String phoneNumber;
}