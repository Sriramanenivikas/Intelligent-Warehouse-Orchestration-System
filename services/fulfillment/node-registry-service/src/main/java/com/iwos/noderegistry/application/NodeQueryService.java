package com.iwos.noderegistry.application;

import com.iwos.noderegistry.api.http.NodeResponse;
import com.iwos.noderegistry.domain.NodeNotFoundException;
import com.iwos.noderegistry.infrastructure.persistence.entity.NodeEntity;
import com.iwos.noderegistry.infrastructure.persistence.repository.NodeRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NodeQueryService {

    private final NodeRepository nodeRepository;

    public NodeQueryService(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    public List<NodeResponse> findNodes(String type, String city, Boolean active) {
        if (type == null && city == null && active == null) {
            return nodeRepository.findAllByOrderByPriorityAscNodeIdAsc().stream()
                    .map(NodeResponse::from)
                    .toList();
        }
        Specification<NodeEntity> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (type != null && !type.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("nodeType")),
                        type.trim().toUpperCase(Locale.ROOT)
                ));
            }
            if (city != null && !city.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("city")),
                        city.trim().toUpperCase(Locale.ROOT)
                ));
            }
            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }
            query.orderBy(criteriaBuilder.asc(root.get("priority")), criteriaBuilder.asc(root.get("nodeId")));
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
        return nodeRepository.findAll(specification).stream()
                .map(NodeResponse::from)
                .toList();
    }

    public NodeResponse getNode(String nodeId) {
        return nodeRepository.findById(nodeId)
                .map(NodeResponse::from)
                .orElseThrow(() -> new NodeNotFoundException(nodeId));
    }
}
