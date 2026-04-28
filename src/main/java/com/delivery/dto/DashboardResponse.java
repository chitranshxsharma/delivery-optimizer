package com.delivery.dto;
import lombok.Data;
@Data
public class DashboardResponse {
    public long totalOrders;
    public long delivered;
    public long inTransit;
    public long slaBreached;
    public long pending;
    public double avgDistanceKm;
    public double avgDeliveryMinutes;
}
