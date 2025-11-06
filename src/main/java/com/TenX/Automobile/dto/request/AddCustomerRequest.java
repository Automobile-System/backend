package com.TenX.Automobile.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCustomerRequest {
    @NotBlank(message = "Name is required")
    private String name;  // Will be split into firstName and lastName

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^(\\+94\\d{9}|0\\d{9})$", message = "Phone number must be local (07XXXXXXXX) or international (+947XXXXXXXX)")
    private String phone;
}

