package com.iwos.review.repository;

import com.iwos.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    Page<Review> findByProductIdAndApprovedTrueOrderByCreatedAtDesc(String productId, Pageable pageable);
    Page<Review> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    Optional<Review> findByProductIdAndUserId(String productId, String userId);
    long countByProductIdAndApprovedTrue(String productId);
}
