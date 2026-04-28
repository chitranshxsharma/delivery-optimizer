package com.delivery.controller;

import com.delivery.dto.ApiResponse;
import com.delivery.dto.TrackingResponse;
import com.delivery.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/track")
@CrossOrigin(origins = "*")
public class TrackingController {

    @Autowired private OrderService orderService;

    /**
     * GET /api/track/{trackingId}
     * PUBLIC endpoint — customers can track their order without logging in.
     * Returns full timeline of status changes (like Delhivery / Shiprocket tracking).
     */
    @GetMapping("/{trackingId}")
    public ResponseEntity<ApiResponse<TrackingResponse>> track(@PathVariable String trackingId) {
        TrackingResponse res = orderService.trackByTrackingId(trackingId);
        return ResponseEntity.ok(ApiResponse.ok("Tracking details", res));
    }
}
