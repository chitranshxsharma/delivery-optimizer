package com.delivery.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Immutable audit event appended every time an order changes status.
 * Powers the live tracking timeline shown to customers / dispatchers.
 */
@Entity
@Table(name = "tracking_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrackingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    private Order.OrderStatus status;

    private String description;       // e.g. "Package picked up from Indore Central Warehouse"
    private String location;          // node name or free-text address
    private LocalDateTime eventTime;

    @PrePersist
    public void prePersist() {
        this.eventTime = LocalDateTime.now();
    }
}
