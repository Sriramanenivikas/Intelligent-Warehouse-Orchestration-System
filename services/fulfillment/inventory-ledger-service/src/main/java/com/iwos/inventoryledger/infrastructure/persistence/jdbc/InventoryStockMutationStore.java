package com.iwos.inventoryledger.infrastructure.persistence.jdbc;

import com.iwos.inventoryledger.domain.inventory.InsufficientStockException;
import com.iwos.inventoryledger.domain.inventory.InventoryStockNotFoundException;
import com.iwos.inventoryledger.domain.inventory.InventoryStockSnapshot;
import com.iwos.inventoryledger.infrastructure.persistence.entity.InventoryStockEntity;
import com.iwos.inventoryledger.infrastructure.persistence.repository.InventoryStockRepository;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryStockMutationStore {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final InventoryStockRepository inventoryStockRepository;

    public InventoryStockMutationStore(
            NamedParameterJdbcTemplate jdbcTemplate,
            InventoryStockRepository inventoryStockRepository
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.inventoryStockRepository = inventoryStockRepository;
    }

    public InventoryStockSnapshot adjustStock(String nodeId, String sku, int quantityDelta) {
        if (quantityDelta < 0 && inventoryStockRepository.findByNodeIdAndSku(nodeId, sku).isEmpty()) {
            throw new InventoryStockNotFoundException(nodeId, sku);
        }

        int updated = jdbcTemplate.update("""
                INSERT INTO inventory_ledger.inventory_stock_items (
                    stock_item_id,
                    node_id,
                    sku,
                    on_hand_quantity,
                    reserved_quantity
                ) VALUES (
                    :stockItemId,
                    :nodeId,
                    :sku,
                    :quantityDelta,
                    0
                )
                ON CONFLICT (node_id, sku) DO UPDATE
                SET on_hand_quantity = inventory_ledger.inventory_stock_items.on_hand_quantity + :quantityDelta,
                    updated_at = CURRENT_TIMESTAMP
                WHERE inventory_ledger.inventory_stock_items.on_hand_quantity + :quantityDelta >= inventory_ledger.inventory_stock_items.reserved_quantity
                  AND inventory_ledger.inventory_stock_items.on_hand_quantity + :quantityDelta >= 0
                """,
                Map.of(
                        "stockItemId", UUID.randomUUID(),
                        "nodeId", nodeId,
                        "sku", sku,
                        "quantityDelta", quantityDelta
                )
        );

        if (updated == 0) {
            throw new InsufficientStockException(nodeId, sku, Math.abs(quantityDelta));
        }

        return findRequired(nodeId, sku);
    }

    public InventoryStockSnapshot reserve(String nodeId, String sku, int quantity) {
        int updated = jdbcTemplate.update("""
                UPDATE inventory_ledger.inventory_stock_items
                SET reserved_quantity = reserved_quantity + :quantity,
                    updated_at = CURRENT_TIMESTAMP
                WHERE node_id = :nodeId
                  AND sku = :sku
                  AND (on_hand_quantity - reserved_quantity) >= :quantity
                """,
                Map.of("nodeId", nodeId, "sku", sku, "quantity", quantity)
        );

        if (updated == 0) {
            if (inventoryStockRepository.findByNodeIdAndSku(nodeId, sku).isEmpty()) {
                throw new InventoryStockNotFoundException(nodeId, sku);
            }
            throw new InsufficientStockException(nodeId, sku, quantity);
        }

        return findRequired(nodeId, sku);
    }

    public InventoryStockSnapshot confirm(String nodeId, String sku, int quantity) {
        int updated = jdbcTemplate.update("""
                UPDATE inventory_ledger.inventory_stock_items
                SET on_hand_quantity = on_hand_quantity - :quantity,
                    reserved_quantity = reserved_quantity - :quantity,
                    updated_at = CURRENT_TIMESTAMP
                WHERE node_id = :nodeId
                  AND sku = :sku
                  AND reserved_quantity >= :quantity
                  AND on_hand_quantity >= :quantity
                """,
                Map.of("nodeId", nodeId, "sku", sku, "quantity", quantity)
        );

        if (updated == 0) {
            throw new InsufficientStockException(nodeId, sku, quantity);
        }

        return findRequired(nodeId, sku);
    }

    public InventoryStockSnapshot release(String nodeId, String sku, int quantity) {
        int updated = jdbcTemplate.update("""
                UPDATE inventory_ledger.inventory_stock_items
                SET reserved_quantity = reserved_quantity - :quantity,
                    updated_at = CURRENT_TIMESTAMP
                WHERE node_id = :nodeId
                  AND sku = :sku
                  AND reserved_quantity >= :quantity
                """,
                Map.of("nodeId", nodeId, "sku", sku, "quantity", quantity)
        );

        if (updated == 0) {
            throw new InsufficientStockException(nodeId, sku, quantity);
        }

        return findRequired(nodeId, sku);
    }

    public InventoryStockSnapshot findRequired(String nodeId, String sku) {
        InventoryStockEntity entity = inventoryStockRepository.findByNodeIdAndSku(nodeId, sku)
                .orElseThrow(() -> new InventoryStockNotFoundException(nodeId, sku));

        return new InventoryStockSnapshot(
                entity.getNodeId(),
                entity.getSku(),
                entity.getOnHandQuantity(),
                entity.getReservedQuantity(),
                entity.getOnHandQuantity() - entity.getReservedQuantity(),
                entity.getUpdatedAt()
        );
    }
}
