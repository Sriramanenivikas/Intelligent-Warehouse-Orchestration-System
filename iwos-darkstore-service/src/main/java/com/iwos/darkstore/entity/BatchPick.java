package com.iwos.darkstore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.List;

@Entity @Table(name = "batch_picks")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BatchPick {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private String storeId;
    @Column(nullable = false) private String pickerId;
    @ElementCollection @Column(name = "order_id")
    private List<String> orderIds;    // Multiple orders picked simultaneously
    @Column(nullable = false) @Builder.Default private Integer totalItems = 0;
    @Enumerated(EnumType.STRING) @Builder.Default
    private BatchPickStatus status = BatchPickStatus.ASSIGNED;
    @CreationTimestamp private Instant createdAt;
    private Instant completedAt;

    public enum BatchPickStatus { ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED }
}
