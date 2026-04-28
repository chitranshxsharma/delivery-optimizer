package com.delivery.repository;

import com.delivery.model.DeliveryNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryNodeRepository extends JpaRepository<DeliveryNode, Long> {
    List<DeliveryNode> findByActiveTrue();
    List<DeliveryNode> findByType(DeliveryNode.NodeType type);
    Optional<DeliveryNode> findByCode(String code);
}
