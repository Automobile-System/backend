package com.TenX.Automobile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOverviewResponse {
    private Integer totalCustomers;
    private Integer newThisMonth;
    private Integer activeCustomers;
    private Double activityRate;
    private TopCustomer topCustomer;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomer {
        private String name;
        private String email;
        private Double totalSpent;
        private Integer servicesUsed;
    }
}

