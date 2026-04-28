package com.delivery.repository;

import com.delivery.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByTrackingId(String trackingId);
    List<Order> findByAssignedDriverId(Long driverId);
    List<Order> findByStatus(Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.status NOT IN ('DELIVERED','FAILED') AND o.slaDeadline < CURRENT_TIMESTAMP")
    List<Order> findSlaBreachedOrders();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'DELIVERED'")
    long countDelivered();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status NOT IN ('DELIVERED','FAILED') AND o.slaDeadline < CURRENT_TIMESTAMP")
    long countSlaBreached();
}
