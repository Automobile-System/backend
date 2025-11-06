package com.TenX.Automobile.dto.request;

import com.TenX.Automobile.enums.JobType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequest {
    
    @NotNull(message = "Job type is required")
    private JobType type;
    
    @NotNull(message = "Type ID is required (Service ID or Project ID)")
    private Long typeId;
    
    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;
    
    private String status;
    
    private LocalDateTime arrivingDate;
    
    private BigDecimal cost;
}
