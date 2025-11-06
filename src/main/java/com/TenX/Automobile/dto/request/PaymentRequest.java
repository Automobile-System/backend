package com.TenX.Automobile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentRequest {
    
    @NotNull(message = "Payment amount is required")
    @Positive(message = "Payment amount must be greater than 0")
    private Double paymentAmount;

    @NotBlank(message = "Payment type is required")
    @Size(max = 50, message = "Payment type must not exceed 50 characters")
    private String paymentType;
}
