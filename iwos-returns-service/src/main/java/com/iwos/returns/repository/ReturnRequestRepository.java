package com.iwos.returns.repository;

import com.iwos.returns.entity.ReturnRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, String> {
    Page<ReturnRequest> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    long countByOrderIdAndStatusNot(String orderId, ReturnRequest.ReturnStatus status);
}
