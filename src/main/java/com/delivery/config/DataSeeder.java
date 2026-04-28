package com.delivery.config;

import com.delivery.model.DeliveryNode;
import com.delivery.model.RouteEdge;
import com.delivery.model.User;
import com.delivery.repository.DeliveryNodeRepository;
import com.delivery.repository.RouteEdgeRepository;
import com.delivery.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds a realistic delivery graph on first startup so the project works out-of-the-box.
 * Graph layout (Indore city simulation):
 *
 *   WH-01 (Warehouse) ─── HUB-01 ─── HUB-02 ─── DP-01..DP-05
 *              └──────────────────────── HUB-03 ─── DP-06..DP-08
 *
 * 10 nodes, 14 edges — enough to demonstrate meaningful Dijkstra savings.
 */
@Component
@Slf4j
public class DataSeeder implements CommandLineRunner {

    @Autowired private DeliveryNodeRepository nodeRepo;
    @Autowired private RouteEdgeRepository    edgeRepo;
    @Autowired private UserRepository         userRepo;
    @Autowired private PasswordEncoder        encoder;

    @Override
    public void run(String... args) {
        if (nodeRepo.count() > 0) {
            log.info("Database already seeded — skipping.");
            return;
        }

        log.info("Seeding delivery graph...");

        // ── Nodes ────────────────────────────────────────────────────────
        DeliveryNode wh01  = save(node("WH-01",  "Indore Central Warehouse",        DeliveryNode.NodeType.WAREHOUSE,      22.7196, 75.8577));
        DeliveryNode hub01 = save(node("HUB-01", "Vijay Nagar Distribution Hub",    DeliveryNode.NodeType.HUB,            22.7533, 75.8937));
        DeliveryNode hub02 = save(node("HUB-02", "Palasia Sorting Hub",             DeliveryNode.NodeType.HUB,            22.7200, 75.8800));
        DeliveryNode hub03 = save(node("HUB-03", "Rajwada Secondary Hub",           DeliveryNode.NodeType.HUB,            22.7185, 75.8490));
        DeliveryNode dp01  = save(node("DP-01",  "Scheme 54 Delivery Point",        DeliveryNode.NodeType.DELIVERY_POINT, 22.7600, 75.9000));
        DeliveryNode dp02  = save(node("DP-02",  "LIG Colony Drop Point",           DeliveryNode.NodeType.DELIVERY_POINT, 22.7650, 75.8870));
        DeliveryNode dp03  = save(node("DP-03",  "Super Corridor Point",            DeliveryNode.NodeType.DELIVERY_POINT, 22.7750, 75.9100));
        DeliveryNode dp04  = save(node("DP-04",  "Annapurna Nagar Delivery",        DeliveryNode.NodeType.DELIVERY_POINT, 22.6900, 75.8700));
        DeliveryNode dp05  = save(node("DP-05",  "Manglia Delivery Point",          DeliveryNode.NodeType.DELIVERY_POINT, 22.6750, 75.9200));
        DeliveryNode dp06  = save(node("DP-06",  "Chhatripura Drop Point",          DeliveryNode.NodeType.DELIVERY_POINT, 22.7100, 75.8400));

        // ── Edges (bidirectional) ─────────────────────────────────────────
        List<RouteEdge> edges = List.of(
            edge(wh01,  hub01, 6.2,  14),
            edge(wh01,  hub02, 3.5,  9),
            edge(wh01,  hub03, 2.8,  7),
            edge(hub01, hub02, 4.0,  10),
            edge(hub01, dp01,  4.5,  11),
            edge(hub01, dp02,  3.8,  9),
            edge(hub01, dp03,  7.1,  17),
            edge(hub02, dp04,  4.2,  10),
            edge(hub02, dp05,  9.0,  22),
            edge(hub02, hub03, 3.3,  8),
            edge(hub03, dp06,  2.5,  6),
            edge(hub03, dp04,  4.8,  12),
            edge(dp01,  dp02,  1.9,  5),
            edge(dp03,  dp05,  5.5,  13)
        );
        edgeRepo.saveAll(edges);

        // ── Users ─────────────────────────────────────────────────────────
        if (!userRepo.existsByEmail("admin@delivery.com")) {
            userRepo.save(User.builder()
                    .name("Admin User")
                    .email("admin@delivery.com")
                    .password(encoder.encode("admin123"))
                    .role(User.UserRole.ROLE_ADMIN)
                    .build());
        }
        if (!userRepo.existsByEmail("dispatcher@delivery.com")) {
            userRepo.save(User.builder()
                    .name("Dispatcher One")
                    .email("dispatcher@delivery.com")
                    .password(encoder.encode("disp123"))
                    .role(User.UserRole.ROLE_DISPATCHER)
                    .build());
        }
        if (!userRepo.existsByEmail("driver@delivery.com")) {
            userRepo.save(User.builder()
                    .name("Driver Ramesh")
                    .email("driver@delivery.com")
                    .password(encoder.encode("driver123"))
                    .role(User.UserRole.ROLE_DRIVER)
                    .build());
        }

        log.info("Seeding complete: {} nodes, {} edges, 3 users", nodeRepo.count(), edgeRepo.count());
    }

    private DeliveryNode save(DeliveryNode n) { return nodeRepo.save(n); }

    private DeliveryNode node(String code, String name, DeliveryNode.NodeType type,
                              double lat, double lng) {
        return DeliveryNode.builder()
                .code(code).name(name).type(type)
                .latitude(lat).longitude(lng)
                .city("Indore").active(true).build();
    }

    private RouteEdge edge(DeliveryNode src, DeliveryNode dst,
                           double distKm, int minutes) {
        return RouteEdge.builder()
                .source(src).destination(dst)
                .distanceKm(distKm).estimatedMinutes(minutes)
                .active(true).bidirectional(true).build();
    }
}
