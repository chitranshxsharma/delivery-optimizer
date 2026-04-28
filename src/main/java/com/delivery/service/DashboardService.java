package com.delivery.service;

import com.delivery.dto.DashboardResponse;
import com.delivery.model.Order;
import com.delivery.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.OptionalDouble;

@Service
public class DashboardService {

    @Autowired private OrderRepository orderRepo;

    public DashboardResponse getSummary() {
        List<Order> all = orderRepo.findAll();

        long delivered  = all.stream().filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED).count();
        long inTransit  = all.stream().filter(o -> o.getStatus() == Order.OrderStatus.IN_TRANSIT).count();
        long pending    = all.stream().filter(o -> o.getStatus() == Order.OrderStatus.PENDING
                                               || o.getStatus() == Order.OrderStatus.ASSIGNED).count();
        long slaBreached = orderRepo.countSlaBreached();

        OptionalDouble avgDist = all.stream()
                .filter(o -> o.getTotalDistanceKm() != null)
                .mapToDouble(Order::getTotalDistanceKm).average();

        OptionalDouble avgMin = all.stream()
                .filter(o -> o.getEstimatedDeliveryMinutes() != null)
                .mapToDouble(Order::getEstimatedDeliveryMinutes).average();

        DashboardResponse res = new DashboardResponse();
        res.totalOrders        = all.size();
        res.delivered          = delivered;
        res.inTransit          = inTransit;
        res.pending            = pending;
        res.slaBreached        = slaBreached;
        res.avgDistanceKm      = avgDist.isPresent()  ? Math.round(avgDist.getAsDouble()  * 100.0) / 100.0 : 0;
        res.avgDeliveryMinutes = avgMin.isPresent()   ? Math.round(avgMin.getAsDouble()   * 10.0)  / 10.0  : 0;
        return res;
    }
}
