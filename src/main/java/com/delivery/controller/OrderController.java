package com.delivery.controller;

import com.delivery.dto.ApiResponse;
import com.delivery.dto.CreateOrderRequest;
import com.delivery.dto.UpdateStatusRequest;
import com.delivery.model.Order;
import com.delivery.security.UserDetailsImpl;
import com.delivery.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired private OrderService orderService;

    /** Create a new order — route is auto-optimised via Dijkstra on creation */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public ResponseEntity<ApiResponse<Order>> create(@Valid @RequestBody CreateOrderRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Order created with optimised route",
                orderService.createOrder(req)));
    }

    /** Update status — enforces the state machine (PENDING→DISPATCHED→IN_TRANSIT→DELIVERED) */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','DRIVER')")
    public ResponseEntity<ApiResponse<Order>> updateStatus(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateStatusRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", orderService.updateStatus(id, req)));
    }

    /** Driver sees their own assigned orders */
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<List<Order>>> myOrders(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.ok("Your orders",
                orderService.getOrdersByDriver(currentUser.getId())));
    }

    /** Admin/Dispatcher: all orders */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public ResponseEntity<ApiResponse<List<Order>>> all() {
        return ResponseEntity.ok(ApiResponse.ok("All orders", orderService.getAllOrders()));
    }

    /** SLA breach alert list — orders past deadline and not delivered */
    @GetMapping("/sla-breached")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public ResponseEntity<ApiResponse<List<Order>>> slaBreached() {
        return ResponseEntity.ok(ApiResponse.ok("SLA breached orders",
                orderService.getSlaBreachedOrders()));
    }
}
