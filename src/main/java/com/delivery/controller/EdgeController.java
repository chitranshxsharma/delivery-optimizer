package com.delivery.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.delivery.dto.ApiResponse;
import com.delivery.dto.EdgeRequest;
import com.delivery.exception.ResourceNotFoundException;
import com.delivery.model.DeliveryNode;
import com.delivery.model.RouteEdge;
import com.delivery.repository.DeliveryNodeRepository;
import com.delivery.repository.RouteEdgeRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/edges")
@CrossOrigin(origins = "*")
public class EdgeController {

    @Autowired private RouteEdgeRepository edgeRepo;
    @Autowired private DeliveryNodeRepository nodeRepo;

    /** Public — frontend graph visualization needs this to draw edges */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAll() {

        List<RouteEdge> edges = edgeRepo.findByActiveTrue();

        List<Map<String, Object>> result = edges.stream().map(e -> {
            Map<String, Object> map = new HashMap<>();

            map.put("id", e.getId());

            if (e.getSource() != null) {
                map.put("sourceNodeId", e.getSource().getId());
                map.put("sourceNodeCode", e.getSource().getCode());
            }

            if (e.getDestination() != null) {
                map.put("destNodeId", e.getDestination().getId());
                map.put("destNodeCode", e.getDestination().getCode());
            }

            map.put("distanceKm", e.getDistanceKm());
            map.put("estimatedMinutes", 
                    e.getEstimatedMinutes() != null ? e.getEstimatedMinutes() : 0);
            map.put("bidirectional", e.isBidirectional());
            map.put("active", e.isActive());

            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok("Active edges", result));
    }

    /** Admin only — add a new edge between two nodes */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@Valid @RequestBody EdgeRequest req) {
        DeliveryNode source = nodeRepo.findById(req.getSourceNodeId())
            .orElseThrow(() -> new ResourceNotFoundException("DeliveryNode", req.getSourceNodeId()));
        DeliveryNode dest = nodeRepo.findById(req.getDestinationNodeId())
            .orElseThrow(() -> new ResourceNotFoundException("DeliveryNode", req.getDestinationNodeId()));

        RouteEdge edge = RouteEdge.builder()
            .source(source)
            .destination(dest)
            .distanceKm(req.getDistanceKm())
            .estimatedMinutes(req.getEstimatedMinutes())
            .bidirectional(req.isBidirectional())
            .active(true)
            .build();

        RouteEdge saved = edgeRepo.save(edge);

        Map<String, Object> response = Map.of(
            "id",               saved.getId(),
            "sourceNodeId",     saved.getSource().getId(),
            "sourceNodeCode",   saved.getSource().getCode(),
            "destNodeId",       saved.getDestination().getId(),
            "destNodeCode",     saved.getDestination().getCode(),
            "distanceKm",       saved.getDistanceKm(),
            "estimatedMinutes", saved.getEstimatedMinutes(),
            "bidirectional",    saved.isBidirectional()
        );
        return ResponseEntity.ok(ApiResponse.ok("Edge created", response));
    }

    /** Admin only — deactivate an edge (soft delete) */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deactivate(@PathVariable Long id) {
        RouteEdge edge = edgeRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RouteEdge", id));
        edge.setActive(false);
        edgeRepo.save(edge);
        return ResponseEntity.ok(ApiResponse.ok("Edge deactivated"));
    }
}
