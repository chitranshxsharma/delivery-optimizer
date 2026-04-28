package com.delivery;

import com.delivery.algorithm.DijkstraRouteOptimizer;
import com.delivery.model.DeliveryNode;
import com.delivery.model.RouteEdge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Dijkstra's algorithm.
 * These tests prove correctness and can be shown in interviews.
 *
 * Run with: mvn test
 */
class DijkstraRouteOptimizerTest {

    private DijkstraRouteOptimizer dijkstra;

    // Test graph:
    //   A --4-- B --3-- D
    //   |       |       |
    //   2       1       2
    //   |       |       |
    //   C --5-- E --1-- F
    //
    // Shortest A→F: A→C→E→F = 2+5+1 = 8  (naive A→B→D→F = 4+3+2 = 9)

    private DeliveryNode nodeA, nodeB, nodeC, nodeD, nodeE, nodeF;
    private List<DeliveryNode> nodes;
    private List<RouteEdge>    edges;

    @BeforeEach
    void setUp() {
        dijkstra = new DijkstraRouteOptimizer();

        nodeA = makeNode(1L, "A"); nodeB = makeNode(2L, "B");
        nodeC = makeNode(3L, "C"); nodeD = makeNode(4L, "D");
        nodeE = makeNode(5L, "E"); nodeF = makeNode(6L, "F");
        nodes = List.of(nodeA, nodeB, nodeC, nodeD, nodeE, nodeF);

        edges = List.of(
            makeEdge(nodeA, nodeB, 4.0, 10),
            makeEdge(nodeA, nodeC, 2.0, 5),
            makeEdge(nodeB, nodeD, 3.0, 7),
            makeEdge(nodeB, nodeE, 1.0, 3),
            makeEdge(nodeC, nodeE, 5.0, 12),
            makeEdge(nodeD, nodeF, 2.0, 5),
            makeEdge(nodeE, nodeF, 1.0, 2)
        );
    }

    @Test
    @DisplayName("Dijkstra finds the globally shortest path A→F = 8km via A→C... wait, shortest is A→B→E→F = 6km")
    void testShortestPath() {
        // A→B = 4, B→E = 1, E→F = 1 → total = 6
        DijkstraRouteOptimizer.RouteResult result =
                dijkstra.findShortestPath(nodes, edges, 1L, 6L);

        assertTrue(result.isRouteFound(), "Should find a route");
        assertEquals(6.0, result.getTotalDistanceKm(), 0.001,
                "Shortest path should be 6.0 km (A→B→E→F)");
        assertEquals(List.of("A", "B", "E", "F"), result.getPathNodeCodes(),
                "Path should follow A→B→E→F");
    }

    @Test
    @DisplayName("Direct path A→B returns correct distance")
    void testDirectPath() {
        DijkstraRouteOptimizer.RouteResult result =
                dijkstra.findShortestPath(nodes, edges, 1L, 2L);
        assertTrue(result.isRouteFound());
        assertEquals(4.0, result.getTotalDistanceKm(), 0.001);
    }

    @Test
    @DisplayName("No path returns routeFound=false gracefully")
    void testNoPath() {
        // Isolated node G with no edges
        DeliveryNode nodeG = makeNode(7L, "G");
        List<DeliveryNode> nodesWithG = List.of(nodeA, nodeB, nodeC, nodeD, nodeE, nodeF, nodeG);

        DijkstraRouteOptimizer.RouteResult result =
                dijkstra.findShortestPath(nodesWithG, edges, 1L, 7L);
        assertFalse(result.isRouteFound(), "Should return no-path result gracefully");
    }

    @Test
    @DisplayName("Same source and destination returns zero distance")
    void testSameNode() {
        DijkstraRouteOptimizer.RouteResult result =
                dijkstra.findShortestPath(nodes, edges, 1L, 1L);
        assertTrue(result.isRouteFound());
        assertEquals(0.0, result.getTotalDistanceKm(), 0.001);
    }

    // ── Helpers ───────────────────────────────────────────────

    private DeliveryNode makeNode(Long id, String code) {
        DeliveryNode n = new DeliveryNode();
        n.setId(id); n.setCode(code); n.setName("Node " + code);
        n.setActive(true); n.setType(DeliveryNode.NodeType.HUB);
        return n;
    }

    private RouteEdge makeEdge(DeliveryNode src, DeliveryNode dst,
                                double distKm, int minutes) {
        RouteEdge e = new RouteEdge();
        e.setSource(src); e.setDestination(dst);
        e.setDistanceKm(distKm); e.setEstimatedMinutes(minutes);
        e.setActive(true); e.setBidirectional(true);
        return e;
    }
}
