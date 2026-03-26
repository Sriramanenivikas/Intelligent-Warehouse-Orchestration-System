package com.iwos.review.service;

import com.iwos.common.exception.BusinessRuleException;
import com.iwos.common.exception.ResourceNotFoundException;
import com.iwos.review.entity.Review;
import com.iwos.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor @Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RatingAggregationService ratingService;
    private final ModerationService moderationService;

    public Review createReview(Review review) {
        reviewRepository.findByProductIdAndUserId(review.getProductId(), review.getUserId())
                .ifPresent(r -> { throw new BusinessRuleException("You have already reviewed this product"); });

        review.setApproved(!moderationService.containsProfanity(review.getContent()));
        Review saved = reviewRepository.save(review);
        ratingService.recalculateRating(review.getProductId());
        log.info("Review created for product {} by user {}", review.getProductId(), review.getUserId());
        return saved;
    }

    public Page<Review> getProductReviews(String productId, Pageable pageable) {
        return reviewRepository.findByProductIdAndApprovedTrueOrderByCreatedAtDesc(productId, pageable);
    }

    public Page<Review> getUserReviews(String userId, Pageable pageable) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Review markHelpful(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        return reviewRepository.save(review);
    }
}
