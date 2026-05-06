package com.iwos.forecasting.infrastructure.persistence.repository;

import com.iwos.forecasting.domain.ForecastDemandSnapshot;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryDemandSignalJdbcRepository {

    private static final String SQL = """
            SELECT
                stock.node_id,
                stock.sku,
                stock.on_hand_quantity,
                stock.reserved_quantity,
                COALESCE(SUM(CASE WHEN reservation.created_at >= NOW() - INTERVAL '1 hour' THEN reservation.quantity ELSE 0 END), 0) AS demand_last_1h,
                COALESCE(SUM(CASE WHEN reservation.created_at >= NOW() - INTERVAL '6 hours' THEN reservation.quantity ELSE 0 END), 0) AS demand_last_6h,
                COALESCE(SUM(CASE WHEN reservation.created_at >= NOW() - INTERVAL '24 hours' THEN reservation.quantity ELSE 0 END), 0) AS demand_last_24h
            FROM inventory_ledger.inventory_stock_items stock
            LEFT JOIN inventory_ledger.inventory_reservations reservation
              ON reservation.node_id = stock.node_id
             AND reservation.sku = stock.sku
             AND reservation.created_at >= NOW() - INTERVAL '24 hours'
            GROUP BY stock.node_id, stock.sku, stock.on_hand_quantity, stock.reserved_quantity
            ORDER BY stock.node_id, stock.sku
            """;

    private final JdbcTemplate jdbcTemplate;

    public InventoryDemandSignalJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ForecastDemandSnapshot> loadDemandSnapshots() {
        return jdbcTemplate.query(SQL, (rs, rowNum) -> new ForecastDemandSnapshot(
                rs.getString("node_id"),
                rs.getString("sku"),
                rs.getInt("on_hand_quantity"),
                rs.getInt("reserved_quantity"),
                rs.getBigDecimal("demand_last_1h") == null ? BigDecimal.ZERO : rs.getBigDecimal("demand_last_1h"),
                rs.getBigDecimal("demand_last_6h") == null ? BigDecimal.ZERO : rs.getBigDecimal("demand_last_6h"),
                rs.getBigDecimal("demand_last_24h") == null ? BigDecimal.ZERO : rs.getBigDecimal("demand_last_24h")
        ));
    }
}
