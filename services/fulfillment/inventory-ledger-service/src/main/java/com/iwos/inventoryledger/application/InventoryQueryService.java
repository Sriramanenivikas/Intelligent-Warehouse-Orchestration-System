package com.iwos.inventoryledger.application;

import com.iwos.inventoryledger.api.http.InventoryReservationResponse;
import com.iwos.inventoryledger.api.http.InventoryStockResponse;
import com.iwos.inventoryledger.domain.inventory.InventoryStockSnapshot;
import com.iwos.inventoryledger.domain.reservation.InventoryReservationNotFoundException;
import com.iwos.inventoryledger.infrastructure.persistence.InventoryResponseMapper;
import com.iwos.inventoryledger.infrastructure.persistence.entity.InventoryReservationEntity;
import com.iwos.inventoryledger.infrastructure.persistence.jdbc.InventoryStockMutationStore;
import com.iwos.inventoryledger.infrastructure.persistence.repository.InventoryReservationRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryQueryService {

    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryStockMutationStore inventoryStockMutationStore;
    private final InventoryResponseMapper inventoryResponseMapper;

    public InventoryQueryService(
            InventoryReservationRepository inventoryReservationRepository,
            InventoryStockMutationStore inventoryStockMutationStore,
            InventoryResponseMapper inventoryResponseMapper
    ) {
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.inventoryStockMutationStore = inventoryStockMutationStore;
        this.inventoryResponseMapper = inventoryResponseMapper;
    }

    @Transactional(readOnly = true)
    public InventoryStockResponse getStock(String nodeId, String sku) {
        InventoryStockSnapshot stockSnapshot = inventoryStockMutationStore.findRequired(normalize(nodeId), normalize(sku));
        return inventoryResponseMapper.toStockResponse(stockSnapshot);
    }

    @Transactional(readOnly = true)
    public InventoryReservationResponse getReservation(UUID reservationId) {
        InventoryReservationEntity reservation = inventoryReservationRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new InventoryReservationNotFoundException(reservationId));
        InventoryStockSnapshot stockSnapshot = inventoryStockMutationStore.findRequired(reservation.getNodeId(), reservation.getSku());
        return inventoryResponseMapper.toReservationResponse(reservation, stockSnapshot);
    }

    private String normalize(String value) {
        return value.trim().toUpperCase();
    }
}
