package com.TenX.Automobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeHistoryResponse {
    private String serviceId;
    private String vehicle;
    private String serviceType;
    private String date;
    private Integer customerRating;
}

