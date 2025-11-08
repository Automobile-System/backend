package com.TenX.Automobile.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddManagerRequest {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^(\\+94\\d{9}|0\\d{9})$", message = "Phone number must be local (07XXXXXXXX) or international (+947XXXXXXXX)")
    private String phone;

    @NotBlank
    @Pattern(regexp = "^MGR-\\d{3}$", message = "Manager ID must be in format MGR-XXX")
    private String managerId;

    @NotBlank
    private String joinDate;  // ISO date string

    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String address;
}

