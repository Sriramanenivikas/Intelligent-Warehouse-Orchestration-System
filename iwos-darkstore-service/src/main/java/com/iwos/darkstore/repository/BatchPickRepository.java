package com.iwos.darkstore.repository;

import com.iwos.darkstore.entity.BatchPick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchPickRepository extends JpaRepository<BatchPick, String> {
}
