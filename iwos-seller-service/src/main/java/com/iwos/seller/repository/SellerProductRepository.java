package com.iwos.seller.repository;

import com.iwos.seller.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerProductRepository extends JpaRepository<SellerProduct, String> {
}
