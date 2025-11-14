package com.TenX.Automobile.dto.request;

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
public class UpdateManagerRequest {
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

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;  // Optional - only if changing password

    private String address;
}
