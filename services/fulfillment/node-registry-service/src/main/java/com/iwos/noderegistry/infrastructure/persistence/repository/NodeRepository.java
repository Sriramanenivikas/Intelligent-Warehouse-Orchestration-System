package com.iwos.noderegistry.infrastructure.persistence.repository;

import com.iwos.noderegistry.infrastructure.persistence.entity.NodeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NodeRepository extends JpaRepository<NodeEntity, String>, JpaSpecificationExecutor<NodeEntity> {

    List<NodeEntity> findAllByOrderByPriorityAscNodeIdAsc();
}
