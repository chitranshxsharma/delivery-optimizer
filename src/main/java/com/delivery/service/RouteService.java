package com.delivery.service;

import com.delivery.algorithm.DijkstraRouteOptimizer;
import com.delivery.algorithm.RouteComparisonService;
import com.delivery.dto.CompareRequest;
import com.delivery.dto.RouteRequest;
import com.delivery.dto.RouteResponse;
import com.delivery.exception.ResourceNotFoundException;
import com.delivery.model.DeliveryNode;
import com.delivery.model.RouteEdge;
import com.delivery.repository.DeliveryNodeRepository;
import com.delivery.repository.RouteEdgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RouteService {

    @Autowired private DijkstraRouteOptimizer dijkstra;
    @Autowired private RouteComparisonService comparison;
    @Autowired private DeliveryNodeRepository nodeRepo;
    @Autowired private RouteEdgeRepository edgeRepo;

    /** Calculate shortest path and return full route details */
    public RouteResponse calculateRoute(RouteRequest req) {
        List<DeliveryNode> allNodes = nodeRepo.findAll();
        List<RouteEdge>    allEdges = edgeRepo.findByActiveTrue();

        DijkstraRouteOptimizer.RouteResult result =
                dijkstra.findShortestPath(allNodes, allEdges, req.getSourceNodeId(), req.getDestinationNodeId());

        RouteResponse res = new RouteResponse();
        res.routeFound = result.isRouteFound();

        if (!result.isRouteFound()) {
            res.message = "No route found between the selected nodes. Check that edges are configured.";
            return res;
        }

        Map<Long, String> nameById = allNodes.stream()
                .collect(Collectors.toMap(DeliveryNode::getId, DeliveryNode::getName));

        res.pathNodeCodes     = result.getPathNodeCodes();
        res.pathNodeNames     = result.getPathNodeIds().stream()
                                    .map(id -> nameById.getOrDefault(id, "Unknown"))
                                    .collect(Collectors.toList());
        res.totalDistanceKm   = result.getTotalDistanceKm();
        res.estimatedMinutes  = result.getEstimatedMinutes();
        res.message           = "Optimal route calculated via Dijkstra's algorithm";
        return res;
    }

    /** Compare naive vs Dijkstra routing across multiple stops — generates the 35% metric */
    public RouteComparisonService.ComparisonResult compareRoutes(CompareRequest req) {
        List<DeliveryNode> allNodes = nodeRepo.findAll();
        List<RouteEdge>    allEdges = edgeRepo.findByActiveTrue();

        List<DeliveryNode> stops = req.getStopNodeIds().stream()
                .map(id -> nodeRepo.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("DeliveryNode", id)))
                .collect(Collectors.toList());

        return comparison.compare(stops, allNodes, allEdges);
    }

    /** Internal use: compute optimised route string for an order */
    public DijkstraRouteOptimizer.RouteResult computeForOrder(Long srcId, Long dstId) {
        List<DeliveryNode> allNodes = nodeRepo.findAll();
        List<RouteEdge>    allEdges = edgeRepo.findByActiveTrue();
        return dijkstra.findShortestPath(allNodes, allEdges, srcId, dstId);
    }
}
