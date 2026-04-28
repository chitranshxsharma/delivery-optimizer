package com.delivery.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Represents a node in the delivery graph.
 * Can be a WAREHOUSE (origin), HUB (intermediate), or DELIVERY_POINT (destination).
 */
@Entity
@Table(name = "delivery_nodes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String code;       // e.g. "WH-01", "HUB-12", "DP-07"

    @NotBlank
    private String name;       // e.g. "Indore Central Warehouse"

    @Enumerated(EnumType.STRING)
    private NodeType type;

    private Double latitude;
    private Double longitude;

    private String city;
    private String address;

    private boolean active = true;

    public enum NodeType {
        WAREHOUSE,
        HUB,
        DELIVERY_POINT
    }
}
