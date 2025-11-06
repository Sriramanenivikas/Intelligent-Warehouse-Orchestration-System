package com.iwos.loadtest.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pincode Entity - Indian postal codes with geolocation
 */
@Entity
@Table(name = "pincodes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pincode {

    @Id
    private String pincode;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "locality")
    private String locality;
}
