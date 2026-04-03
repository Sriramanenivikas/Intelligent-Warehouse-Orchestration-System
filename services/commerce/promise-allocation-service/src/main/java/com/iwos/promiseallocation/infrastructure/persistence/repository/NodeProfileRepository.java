package com.iwos.promiseallocation.infrastructure.persistence.repository;

import com.iwos.promiseallocation.infrastructure.persistence.entity.NodeProfileEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodeProfileRepository extends JpaRepository<NodeProfileEntity, String> {

    List<NodeProfileEntity> findByActiveTrueOrderByPriorityAsc();

    long countByActiveTrue();
}
