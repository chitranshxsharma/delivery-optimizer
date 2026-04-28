package com.delivery.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class EdgeRequest {
    @NotNull public Long sourceNodeId;
    @NotNull public Long destinationNodeId;
    @NotNull @DecimalMin("0.1") public Double distanceKm;
    @NotNull @Min(1) public Integer estimatedMinutes;
    public boolean bidirectional = true;
}
