package com.delivery.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A customer delivery order.
 * Tracks the full lifecycle: PENDING → ASSIGNED → DISPATCHED → IN_TRANSIT → DELIVERED | FAILED
 */
@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String trackingId;           // e.g. "TRK-20240422-001"

    private String customerName;
    private String customerPhone;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_node_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private DeliveryNode sourceNode;     // pickup / warehouse

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_node_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private DeliveryNode destinationNode; // delivery point

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_driver_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User assignedDriver;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    /** JSON-serialised list of node codes representing the optimised path */
    @Column(columnDefinition = "TEXT")
    private String optimisedRoutePath;

    private Double totalDistanceKm;
    private Integer estimatedDeliveryMinutes;

    /** SLA deadline — breached if not delivered by this time */
    private LocalDateTime slaDeadline;

    private LocalDateTime createdAt;
    private LocalDateTime dispatchedAt;
    private LocalDateTime deliveredAt;

    private String notes;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
        // Default SLA: 4 hours from creation
        this.slaDeadline = this.createdAt.plusHours(4);
    }

    public boolean isSlaBreached() {
        return status != OrderStatus.DELIVERED
                && LocalDateTime.now().isAfter(slaDeadline);
    }

    public enum OrderStatus {
        PENDING,
        ASSIGNED,
        DISPATCHED,
        IN_TRANSIT,
        DELIVERED,
        FAILED
    }
}
