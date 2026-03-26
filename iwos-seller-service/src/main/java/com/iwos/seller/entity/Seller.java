package com.iwos.seller.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;

@Entity
@Table(name = "sellers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Seller {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false) private String userId;
    @Column(nullable = false) private String businessName;
    @Column(nullable = false, unique = true, length = 15) private String gstin;
    @Column(nullable = false, length = 10) private String panNumber;
    @Column(nullable = false) private String bankAccountNumber;
    @Column(nullable = false) private String ifscCode;
    private String bankName;
    @Column(columnDefinition = "TEXT") private String address;
    private String city;
    private String state;
    private String pincode;
    @Column(length = 15) private String phone;
    @Enumerated(EnumType.STRING) @Builder.Default
    private SellerStatus status = SellerStatus.PENDING_VERIFICATION;
    @Builder.Default private Double commissionRate = 10.0;
    @CreationTimestamp private Instant createdAt;
    @UpdateTimestamp private Instant updatedAt;

    public enum SellerStatus { PENDING_VERIFICATION, ACTIVE, SUSPENDED, DEACTIVATED }
}
