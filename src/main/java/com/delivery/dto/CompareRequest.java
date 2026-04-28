package com.delivery.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
@Data
public class CompareRequest {
    @NotNull public List<Long> stopNodeIds;
}
