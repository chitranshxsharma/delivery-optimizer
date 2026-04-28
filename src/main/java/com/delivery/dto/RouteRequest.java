package com.delivery.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class RouteRequest {
    @NotNull public Long sourceNodeId;
    @NotNull public Long destinationNodeId;
}
