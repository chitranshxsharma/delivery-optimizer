package com.delivery.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class CreateOrderRequest {
    @NotBlank public String customerName;
    @NotBlank public String customerPhone;
    @NotNull  public Long   sourceNodeId;
    @NotNull  public Long   destinationNodeId;
    public Long assignedDriverId;
    public String notes;
    public Integer slaHours;
}
