package com.iwos.returns.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "quality_check_results")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class QualityCheckResult {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private String returnRequestId;
    @Enumerated(EnumType.STRING) private QcVerdict verdict;
    @Column(columnDefinition = "TEXT") private String notes;
    private String inspectorId;
    private Instant checkedAt;

    public enum QcVerdict { APPROVED_FULL_REFUND, APPROVED_PARTIAL_REFUND, REJECTED_DAMAGED_BY_CUSTOMER, REJECTED_MISSING_PARTS }
}
