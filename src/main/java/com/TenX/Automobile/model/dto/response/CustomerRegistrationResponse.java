package com.TenX.Automobile.model.dto.response;


import lombok.*;
import lombok.experimental.SuperBuilder;



@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegistrationResponse extends UserRegistrationResponse {

    private String customerId;

}
