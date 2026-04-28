package com.delivery.dto;
import lombok.Data;
import java.util.List;
@Data
public class RouteResponse {
    public boolean routeFound;
    public List<String> pathNodeCodes;
    public List<String> pathNodeNames;
    public double totalDistanceKm;
    public int estimatedMinutes;
    public String message;
}
