package com.delivery.service;

import com.delivery.dto.NodeRequest;
import com.delivery.dto.NodeResponse;
import com.delivery.exception.BadRequestException;
import com.delivery.exception.ResourceNotFoundException;
import com.delivery.model.DeliveryNode;
import com.delivery.repository.DeliveryNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NodeService {

    @Autowired
    private DeliveryNodeRepository nodeRepo;

    public List<NodeResponse> getAllActive() {
        return nodeRepo.findByActiveTrue().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<NodeResponse> getAll() {
        return nodeRepo.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public NodeResponse getById(Long id) {
        return toResponse(nodeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryNode", id)));
    }

    public NodeResponse create(NodeRequest req) {
        nodeRepo.findByCode(req.getCode()).ifPresent(n -> {
            throw new BadRequestException("Node with code '" + req.getCode() + "' already exists");
        });
        DeliveryNode node = DeliveryNode.builder()
                .code(req.getCode()).name(req.getName()).type(req.getType())
                .latitude(req.getLatitude()).longitude(req.getLongitude())
                .city(req.getCity()).address(req.getAddress()).active(true)
                .build();
        return toResponse(nodeRepo.save(node));
    }

    public NodeResponse update(Long id, NodeRequest req) {
        DeliveryNode node = nodeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryNode", id));
        node.setName(req.getName());
        node.setType(req.getType());
        node.setLatitude(req.getLatitude());
        node.setLongitude(req.getLongitude());
        node.setCity(req.getCity());
        node.setAddress(req.getAddress());
        return toResponse(nodeRepo.save(node));
    }

    public void deactivate(Long id) {
        DeliveryNode node = nodeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryNode", id));
        node.setActive(false);
        nodeRepo.save(node);
    }

    public DeliveryNode getEntityById(Long id) {
        return nodeRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("DeliveryNode", id));
    }

    private NodeResponse toResponse(DeliveryNode n) {
        NodeResponse r = new NodeResponse();
        r.id = n.getId(); r.code = n.getCode(); r.name = n.getName();
        r.type = n.getType().name(); r.latitude = n.getLatitude();
        r.longitude = n.getLongitude(); r.city = n.getCity(); r.active = n.isActive();
        return r;
    }
}
