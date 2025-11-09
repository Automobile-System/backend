package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopEmployeeResponse {
    private String id;
    private String name;
    private String specialization;
    private Double rating;
    private Boolean rewardEligible;
    private Boolean overloaded;
}

