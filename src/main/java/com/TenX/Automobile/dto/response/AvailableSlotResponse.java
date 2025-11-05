package com.TenX.Automobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableSlotResponse {
    private LocalDate date;
    private Integer totalCapacity;
    private Integer bookedCount;
    private Integer availableCount;
    private Boolean isAvailable;
}
