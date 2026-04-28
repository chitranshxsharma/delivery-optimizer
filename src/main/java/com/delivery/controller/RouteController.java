package com.delivery.controller;

import com.delivery.algorithm.RouteComparisonService;
import com.delivery.dto.ApiResponse;
import com.delivery.dto.CompareRequest;
import com.delivery.dto.RouteRequest;
import com.delivery.dto.RouteResponse;
import com.delivery.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*")
public class RouteController {

    @Autowired private RouteService routeService;

    /**
     * POST /api/routes/optimise
     * Body: { "sourceNodeId": 1, "destinationNodeId": 5 }
     *
     * Returns the Dijkstra-optimised path, total distance and estimated time.
     * This is the core algorithm endpoint — highlight this in interviews.
     */
    @PostMapping("/optimise")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','DRIVER')")
    public ResponseEntity<ApiResponse<RouteResponse>> optimise(@Valid @RequestBody RouteRequest req) {
        RouteResponse result = routeService.calculateRoute(req);
        return ResponseEntity.ok(ApiResponse.ok("Route calculated", result));
    }

    /**
     * POST /api/routes/compare
     * Body: { "stopNodeIds": [1, 3, 7, 5, 9] }
     *
     * Compares naive sequential routing vs Dijkstra + nearest-neighbour reorder.
     * Use this to generate the 35% improvement metric for JMeter screenshots.
     */
    @PostMapping("/compare")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public ResponseEntity<ApiResponse<RouteComparisonService.ComparisonResult>> compare(
            @Valid @RequestBody CompareRequest req) {
        RouteComparisonService.ComparisonResult result = routeService.compareRoutes(req);
        return ResponseEntity.ok(ApiResponse.ok("Route comparison complete", result));
    }
}
