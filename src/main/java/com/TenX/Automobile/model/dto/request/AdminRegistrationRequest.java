package com.TenX.Automobile.model.dto.request;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class AdminRegistrationRequest extends BaseUserRegistrationRequest{
    private String adminId;
}
