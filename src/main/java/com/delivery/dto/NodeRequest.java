package com.delivery.dto;
import com.delivery.model.DeliveryNode;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class NodeRequest {
    @NotBlank public String code;
    @NotBlank public String name;
    @NotNull  public DeliveryNode.NodeType type;
    public Double latitude;
    public Double longitude;
    public String city;
    public String address;
}
