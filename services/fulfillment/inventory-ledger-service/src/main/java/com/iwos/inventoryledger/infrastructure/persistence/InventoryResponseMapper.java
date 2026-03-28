package com.iwos.inventoryledger.infrastructure.persistence;

import com.iwos.inventoryledger.api.http.InventoryReservationResponse;
import com.iwos.inventoryledger.api.http.InventoryStockResponse;
import com.iwos.inventoryledger.api.http.StockAdjustmentResponse;
import com.iwos.inventoryledger.domain.inventory.InventoryStockSnapshot;
import com.iwos.inventoryledger.infrastructure.persistence.entity.InventoryLedgerEntryEntity;
import com.iwos.inventoryledger.infrastructure.persistence.entity.InventoryReservationEntity;
import org.springframework.stereotype.Component;

@Component
public class InventoryResponseMapper {

    public InventoryStockResponse toStockResponse(InventoryStockSnapshot stockSnapshot) {
        return new InventoryStockResponse(
                stockSnapshot.nodeId(),
                stockSnapshot.sku(),
                stockSnapshot.onHandQuantity(),
                stockSnapshot.reservedQuantity(),
                stockSnapshot.availableQuantity(),
                stockSnapshot.updatedAt()
        );
    }

    public StockAdjustmentResponse toStockAdjustmentResponse(
            InventoryLedgerEntryEntity ledgerEntry,
            InventoryStockSnapshot stockSnapshot
    ) {
        return new StockAdjustmentResponse(
                ledgerEntry.getLedgerEntryId(),
                ledgerEntry.getNodeId(),
                ledgerEntry.getSku(),
                ledgerEntry.getOnHandDelta(),
                ledgerEntry.getReason(),
                ledgerEntry.getReferenceType(),
                ledgerEntry.getReferenceId(),
                toStockResponse(stockSnapshot),
                ledgerEntry.getCreatedAt()
        );
    }

    public InventoryReservationResponse toReservationResponse(
            InventoryReservationEntity reservation,
            InventoryStockSnapshot stockSnapshot
    ) {
        return new InventoryReservationResponse(
                reservation.getReservationId(),
                reservation.getOrderReference(),
                reservation.getNodeId(),
                reservation.getSku(),
                reservation.getQuantity(),
                reservation.getStatus(),
                reservation.getExpiresAt(),
                reservation.getCreatedAt(),
                toStockResponse(stockSnapshot)
        );
    }
}
