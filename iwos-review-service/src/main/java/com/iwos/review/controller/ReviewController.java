package com.iwos.review.controller;

import com.iwos.review.entity.RatingSnapshot;
import com.iwos.review.entity.Review;
import com.iwos.review.service.RatingAggregationService;
import com.iwos.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/reviews") @RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final RatingAggregationService ratingService;

    @PostMapping
    public ResponseEntity<Review> create(@RequestBody Review review) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(review));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<Review>> getProductReviews(@PathVariable String productId, Pageable pageable) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId, pageable));
    }

    @GetMapping("/product/{productId}/rating")
    public ResponseEntity<RatingSnapshot> getRating(@PathVariable String productId) {
        return ResponseEntity.ok(ratingService.getRatingSnapshot(productId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Review>> getUserReviews(@PathVariable String userId, Pageable pageable) {
        return ResponseEntity.ok(reviewService.getUserReviews(userId, pageable));
    }

    @PostMapping("/{id}/helpful")
    public ResponseEntity<Review> markHelpful(@PathVariable String id) {
        return ResponseEntity.ok(reviewService.markHelpful(id));
    }
}
