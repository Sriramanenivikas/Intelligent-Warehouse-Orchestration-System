package com.iwos.returns.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "return_requests")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReturnRequest {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private String orderId;
    @Column(nullable = false) private String userId;
    @Column(nullable = false, length = 50) @Enumerated(EnumType.STRING)
    private ReturnReason reason;
    @Column(columnDefinition = "TEXT") private String description;
    @Enumerated(EnumType.STRING) @Builder.Default
    private ReturnStatus status = ReturnStatus.REQUESTED;
    @Column(precision = 12, scale = 2) private BigDecimal refundAmount;
    private String pickupAddress;
    private Instant pickupScheduledAt;
    private Instant pickedUpAt;
    private Instant receivedAt;
    private Instant refundedAt;
    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<ReturnItem> items = new ArrayList<>();
    @CreationTimestamp private Instant createdAt;

    public enum ReturnReason { DEFECTIVE, WRONG_ITEM, SIZE_ISSUE, DAMAGED_IN_TRANSIT, NOT_AS_DESCRIBED, CHANGED_MIND, OTHER }
    public enum ReturnStatus { REQUESTED, APPROVED, PICKUP_SCHEDULED, PICKED_UP, RECEIVED, QUALITY_CHECK, REFUND_INITIATED, REFUNDED, REJECTED }
}
