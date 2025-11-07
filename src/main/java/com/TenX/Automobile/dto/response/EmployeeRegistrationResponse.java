package com.TenX.Automobile.dto.response;

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeRegistrationResponse extends UserRegistrationResponse {
    private String employeeId;
    private String specialty;
}
