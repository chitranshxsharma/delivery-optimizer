package com.delivery.controller;

import com.delivery.dto.ApiResponse;
import com.delivery.dto.NodeRequest;
import com.delivery.dto.NodeResponse;
import com.delivery.service.NodeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nodes")
@CrossOrigin(origins = "*")
public class NodeController {

    @Autowired private NodeService nodeService;

    /** Public — all apps can query nodes for dropdowns */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<NodeResponse>>> getActive() {
        return ResponseEntity.ok(ApiResponse.ok("Active nodes", nodeService.getAllActive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NodeResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Node details", nodeService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public ResponseEntity<ApiResponse<List<NodeResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("All nodes", nodeService.getAll()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NodeResponse>> create(@Valid @RequestBody NodeRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Node created", nodeService.create(req)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NodeResponse>> update(@PathVariable Long id,
                                                             @Valid @RequestBody NodeRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Node updated", nodeService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deactivate(@PathVariable Long id) {
        nodeService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok("Node deactivated"));
    }
}
