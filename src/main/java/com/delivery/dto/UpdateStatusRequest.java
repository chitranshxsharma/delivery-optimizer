package com.delivery.dto;
import com.delivery.model.Order;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class UpdateStatusRequest {
    @NotNull public Order.OrderStatus status;
    public String description;
    public String location;
}
