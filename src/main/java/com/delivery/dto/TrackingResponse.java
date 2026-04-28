package com.delivery.dto;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class TrackingResponse {
    public String trackingId;
    public String status;
    public boolean slaBreached;
    public LocalDateTime slaDeadline;
    public String optimisedRoute;
    public Double totalDistanceKm;
    public Integer estimatedMinutes;
    public List<TrackingEventDto> timeline;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class TrackingEventDto {
        public String status;
        public String description;
        public String location;
        public LocalDateTime eventTime;
    }
}
