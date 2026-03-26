package com.iwos.darkstore.repository;

import com.iwos.darkstore.entity.ReplenishmentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReplenishmentOrderRepository extends JpaRepository<ReplenishmentOrder, String> {
    List<ReplenishmentOrder> findByStoreIdAndStatus(String storeId, ReplenishmentOrder.ReplenishmentStatus status);
}
