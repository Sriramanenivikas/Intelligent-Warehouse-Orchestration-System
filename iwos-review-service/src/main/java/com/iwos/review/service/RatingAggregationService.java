package com.iwos.review.service;

import com.iwos.review.entity.RatingSnapshot;
import com.iwos.review.entity.Review;
import com.iwos.review.repository.RatingSnapshotRepository;
import com.iwos.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service @RequiredArgsConstructor
public class RatingAggregationService {

    private final ReviewRepository reviewRepository;
    private final RatingSnapshotRepository snapshotRepository;

    public void recalculateRating(String productId) {
        List<Review> reviews = reviewRepository
                .findByProductIdAndApprovedTrueOrderByCreatedAtDesc(productId, Pageable.unpaged()).getContent();

        if (reviews.isEmpty()) return;

        Map<Integer, Integer> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) distribution.put(i, 0);

        double sum = 0;
        for (Review r : reviews) {
            sum += r.getRating();
            distribution.merge(r.getRating(), 1, Integer::sum);
        }

        BigDecimal avg = BigDecimal.valueOf(sum / reviews.size()).setScale(2, RoundingMode.HALF_UP);

        RatingSnapshot snapshot = RatingSnapshot.builder()
                .productId(productId)
                .averageRating(avg)
                .totalReviews(reviews.size())
                .distribution(distribution)
                .build();
        snapshotRepository.save(snapshot);
    }

    public RatingSnapshot getRatingSnapshot(String productId) {
        return snapshotRepository.findById(productId).orElse(
                RatingSnapshot.builder().productId(productId)
                        .averageRating(BigDecimal.ZERO).totalReviews(0).distribution(Map.of()).build());
    }
}
