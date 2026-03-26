package com.iwos.review.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Map;

@Document(collection = "rating_snapshots")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RatingSnapshot {
    @Id private String productId;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Map<Integer, Integer> distribution;  // {5: 120, 4: 80, 3: 30, 2: 10, 1: 5}
}
