package com.TenX.Automobile.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceBookingRequest {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Arriving date is required")
    private LocalDateTime arrivingDate;

    @NotNull(message = "At least one vehicle is required")
    private List<UUID> vehicleIds;

    private String additionalNotes;
}
