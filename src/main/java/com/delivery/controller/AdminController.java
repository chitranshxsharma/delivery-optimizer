package com.delivery.controller;

import com.delivery.dto.ApiResponse;
import com.delivery.dto.DashboardResponse;
import com.delivery.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired private DashboardService dashboardService;

    /**
     * GET /api/admin/dashboard
     * Returns order stats, SLA breach count, avg distance saved.
     * Perfect for a recruiter demo — shows real impact numbers.
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok("Dashboard summary", dashboardService.getSummary()));
    }
}
