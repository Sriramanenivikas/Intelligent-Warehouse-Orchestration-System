package com.iwos.review.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "reviews")
@CompoundIndex(name = "idx_product_user", def = "{'productId': 1, 'userId': 1}", unique = true)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Review {
    @Id private String id;
    @Indexed private String productId;
    @Indexed private String userId;
    private String userName;
    private String orderId;
    private Integer rating;   // 1-5
    private String title;
    private String content;
    private List<String> imageUrls;
    @Builder.Default private boolean verifiedPurchase = false;
    @Builder.Default private boolean approved = true;
    @Builder.Default private Integer helpfulCount = 0;
    @Builder.Default private Integer reportCount = 0;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;
}
