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
public class AddEmployeeRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^(\\+94\\d{9}|0\\d{9})$", message = "Phone number must be local (07XXXXXXXX) or international (+947XXXXXXXX)")
    private String phone;

    @NotBlank
    @Pattern(regexp = "^EMP-\\d{3}$", message = "Employee ID must be in format EMP-XXX")
    private String employeeId;

    @NotBlank(message = "Specialization is required")
    private String specialization;  // See specialization list in documentation

    @NotBlank(message = "Join date is required")
    private String joinDate;  // ISO date string

    @NotBlank(message = "Salary is required")
    private String salary;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String experience;
    private String address;
}

