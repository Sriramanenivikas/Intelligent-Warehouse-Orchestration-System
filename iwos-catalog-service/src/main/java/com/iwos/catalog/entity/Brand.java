package com.iwos.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "brands")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(unique = true, length = 120)
    private String slug;

    private String logoUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
