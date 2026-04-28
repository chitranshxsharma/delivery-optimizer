package com.delivery.service;

import com.delivery.algorithm.DijkstraRouteOptimizer;
import com.delivery.dto.CreateOrderRequest;
import com.delivery.dto.TrackingResponse;
import com.delivery.dto.UpdateStatusRequest;
import com.delivery.exception.BadRequestException;
import com.delivery.exception.ResourceNotFoundException;
import com.delivery.model.*;
import com.delivery.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired private OrderRepository         orderRepo;
    @Autowired private TrackingEventRepository trackingRepo;
    @Autowired private UserRepository          userRepo;
    @Autowired private DeliveryNodeRepository  nodeRepo;
    @Autowired private RouteService            routeService;

    @Transactional
    public Order createOrder(CreateOrderRequest req) {
        DeliveryNode src = nodeRepo.findById(req.getSourceNodeId())
                .orElseThrow(() -> new ResourceNotFoundException("Source node", req.getSourceNodeId()));
        DeliveryNode dst = nodeRepo.findById(req.getDestinationNodeId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination node", req.getDestinationNodeId()));

        // Calculate optimised route immediately on order creation
        DijkstraRouteOptimizer.RouteResult route =
                routeService.computeForOrder(src.getId(), dst.getId());

        if (!route.isRouteFound()) {
            throw new BadRequestException("No delivery route exists between '"
                    + src.getName() + "' and '" + dst.getName() + "'");
        }

        Order order = Order.builder()
                .trackingId("TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customerName(req.getCustomerName())
                .customerPhone(req.getCustomerPhone())
                .sourceNode(src)
                .destinationNode(dst)
                .optimisedRoutePath(String.join(" → ", route.getPathNodeCodes()))
                .totalDistanceKm(route.getTotalDistanceKm())
                .estimatedDeliveryMinutes(route.getEstimatedMinutes())
                .notes(req.getNotes())
                .build();

        if (req.getSlaHours() != null && req.getSlaHours() > 0) {
            order.setSlaDeadline(LocalDateTime.now().plusHours(req.getSlaHours()));
        }

        if (req.getAssignedDriverId() != null) {
            User driver = userRepo.findById(req.getAssignedDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Driver", req.getAssignedDriverId()));
            order.setAssignedDriver(driver);
            order.setStatus(Order.OrderStatus.ASSIGNED);
        }

        Order saved = orderRepo.save(order);

        // First tracking event
        appendTrackingEvent(saved, Order.OrderStatus.PENDING,
                "Order created. Optimised route: " + saved.getOptimisedRoutePath(),
                src.getName());

        return saved;
    }

    @Transactional
    public Order updateStatus(Long orderId, UpdateStatusRequest req) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        validateStatusTransition(order.getStatus(), req.getStatus());

        order.setStatus(req.getStatus());
        if (req.getStatus() == Order.OrderStatus.DISPATCHED)
            order.setDispatchedAt(LocalDateTime.now());
        if (req.getStatus() == Order.OrderStatus.DELIVERED)
            order.setDeliveredAt(LocalDateTime.now());

        Order saved = orderRepo.save(order);
        appendTrackingEvent(saved, req.getStatus(), req.getDescription(), req.getLocation());
        return saved;
    }

    public TrackingResponse trackByTrackingId(String trackingId) {
        Order order = orderRepo.findByTrackingId(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Order with tracking ID: " + trackingId));

        List<TrackingEvent> events = trackingRepo.findByOrderIdOrderByEventTimeAsc(order.getId());

        TrackingResponse res = new TrackingResponse();
        res.trackingId        = order.getTrackingId();
        res.status            = order.getStatus().name();
        res.slaBreached       = order.isSlaBreached();
        res.slaDeadline       = order.getSlaDeadline();
        res.optimisedRoute    = order.getOptimisedRoutePath();
        res.totalDistanceKm   = order.getTotalDistanceKm();
        res.estimatedMinutes  = order.getEstimatedDeliveryMinutes();
        res.timeline = events.stream().map(e -> new TrackingResponse.TrackingEventDto(
                e.getStatus().name(), e.getDescription(), e.getLocation(), e.getEventTime()
        )).collect(Collectors.toList());

        return res;
    }

    public List<Order> getOrdersByDriver(Long driverId) {
        return orderRepo.findByAssignedDriverId(driverId);
    }

    public List<Order> getAllOrders() {
        return orderRepo.findAll();
    }

    public List<Order> getSlaBreachedOrders() {
        return orderRepo.findSlaBreachedOrders();
    }

    // ── Helpers ───────────────────────────────────────────────

    private void appendTrackingEvent(Order order, Order.OrderStatus status,
                                     String description, String location) {
        TrackingEvent event = TrackingEvent.builder()
                .order(order)
                .status(status)
                .description(description != null ? description : status.name())
                .location(location)
                .build();
        trackingRepo.save(event);
    }

    /**
     * State machine guard — prevents illegal status transitions.
     * Valid transitions:
     *   PENDING → ASSIGNED | DISPATCHED | FAILED
     *   ASSIGNED → DISPATCHED | FAILED
     *   DISPATCHED → IN_TRANSIT | FAILED
     *   IN_TRANSIT → DELIVERED | FAILED
     */
    private void validateStatusTransition(Order.OrderStatus current, Order.OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING    -> next == Order.OrderStatus.ASSIGNED
                            || next == Order.OrderStatus.DISPATCHED
                            || next == Order.OrderStatus.FAILED;
            case ASSIGNED   -> next == Order.OrderStatus.DISPATCHED
                            || next == Order.OrderStatus.FAILED;
            case DISPATCHED -> next == Order.OrderStatus.IN_TRANSIT
                            || next == Order.OrderStatus.FAILED;
            case IN_TRANSIT -> next == Order.OrderStatus.DELIVERED
                            || next == Order.OrderStatus.FAILED;
            default -> false;
        };
        if (!valid)
            throw new BadRequestException("Invalid status transition: " + current + " → " + next);
    }
}
