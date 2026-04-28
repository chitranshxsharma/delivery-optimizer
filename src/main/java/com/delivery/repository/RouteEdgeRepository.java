package com.delivery.repository;

import com.delivery.model.RouteEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteEdgeRepository extends JpaRepository<RouteEdge, Long> {
    List<RouteEdge> findByActiveTrue();

    @Query("SELECT e FROM RouteEdge e WHERE e.source.id = :sourceId AND e.active = true")
    List<RouteEdge> findActiveEdgesFromSource(Long sourceId);

    @Query("SELECT e FROM RouteEdge e WHERE (e.source.id = :nodeId OR e.destination.id = :nodeId) AND e.active = true")
    List<RouteEdge> findActiveEdgesForNode(Long nodeId);
}
