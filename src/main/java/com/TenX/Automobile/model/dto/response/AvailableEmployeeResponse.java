package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableEmployeeResponse {
    private String id;
    private String name;
    private String skill;
    private String tasks;
    private Boolean disabled;
}

