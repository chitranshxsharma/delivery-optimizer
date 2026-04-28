package com.delivery.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * A directed edge between two DeliveryNodes.
 * distanceKm and estimatedMinutes are the weights used by Dijkstra's algorithm.
 */
@Entity
@Table(name = "route_edges")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RouteEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_node_id", nullable = false)
    private DeliveryNode source;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_node_id", nullable = false)
    private DeliveryNode destination;

    /** Distance in kilometres — primary weight for route optimisation */
    private Double distanceKm;

    /** Estimated travel time in minutes — used for SLA calculations */
    private Integer estimatedMinutes;

    /** Whether this road/route is currently passable */
    private boolean active = true;

    /** Bidirectional flag — if true, edge applies both ways */
    private boolean bidirectional = true;
}
