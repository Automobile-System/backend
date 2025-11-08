package com.TenX.Automobile.model.dto.request;

import com.TenX.Automobile.model.enums.EmployeeType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeRegistrationRequest extends BaseUserRegistrationRequest {

    @Size(max = 50)
    private String speciality;

    @Enumerated(EnumType.STRING)
    private EmployeeType employeeType;

}
