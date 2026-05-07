package com.iwos.controltower.infrastructure.persistence.repository;

import com.iwos.controltower.api.http.ControlTowerSnapshotResponse.BucketCountResponse;
import com.iwos.controltower.api.http.ControlTowerSnapshotResponse.ControlTowerExceptionResponse;
import com.iwos.controltower.api.http.ControlTowerSnapshotResponse.ControlTowerForecastAlertResponse;
import com.iwos.controltower.api.http.ControlTowerSnapshotResponse.ForecastKpiResponse;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ControlTowerReadModelJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public ControlTowerReadModelJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ForecastKpiResponse loadForecastKpi() {
        String sql = """
                SELECT
                    COUNT(*) AS total_forecasts,
                    COUNT(*) FILTER (WHERE stockout_risk = 'CRITICAL') AS critical_count,
                    COUNT(*) FILTER (WHERE stockout_risk = 'HIGH') AS high_count,
                    COUNT(*) FILTER (WHERE stockout_risk = 'MEDIUM') AS medium_count,
                    COUNT(*) FILTER (WHERE stockout_risk = 'LOW') AS low_count,
                    COALESCE(SUM(recommended_replenishment_quantity), 0) AS total_recommended_replenishment_quantity
                FROM forecasting_planning.inventory_forecasts
                """;
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new ForecastKpiResponse(
                rs.getInt("total_forecasts"),
                rs.getLong("critical_count"),
                rs.getLong("high_count"),
                rs.getLong("medium_count"),
                rs.getLong("low_count"),
                rs.getInt("total_recommended_replenishment_quantity")
        ));
    }

    public List<BucketCountResponse> loadOrderIntentStatuses() {
        return countBy("public.order_intents", "status");
    }

    public List<BucketCountResponse> loadFulfillmentOrderStatuses() {
        return countBy("warehouse_orchestration.fulfillment_orders", "status");
    }

    public List<BucketCountResponse> loadShipmentStatuses() {
        return countBy("shipment_handoff.shipments", "status");
    }

    public List<BucketCountResponse> loadNetworkShipmentStatuses() {
        return countBy("shipment_network.network_shipments", "status");
    }

    public List<BucketCountResponse> loadScanEventTypes() {
        return countBy("scan_event.normalized_scan_events", "scan_type");
    }

    public List<BucketCountResponse> loadNotificationAudienceCounts() {
        return countBy("notification.notifications", "audience");
    }

    public List<BucketCountResponse> loadNotificationStatusCounts() {
        return countBy("notification.notifications", "status");
    }

    public List<ControlTowerForecastAlertResponse> loadTopForecastAlerts(int limit) {
        String sql = """
                SELECT forecast_id, forecast_run_id, node_id, sku, available_quantity, predicted_15m_demand,
                       predicted_24h_demand, days_of_cover, stockout_risk, recommended_replenishment_quantity
                FROM forecasting_planning.inventory_forecasts
                ORDER BY
                    CASE stockout_risk
                        WHEN 'CRITICAL' THEN 1
                        WHEN 'HIGH' THEN 2
                        WHEN 'MEDIUM' THEN 3
                        ELSE 4
                    END,
                    recommended_replenishment_quantity DESC,
                    days_of_cover ASC,
                    generated_at DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ControlTowerForecastAlertResponse(
                rs.getObject("forecast_id", UUID.class),
                rs.getObject("forecast_run_id", UUID.class),
                rs.getString("node_id"),
                rs.getString("sku"),
                rs.getBigDecimal("available_quantity"),
                rs.getBigDecimal("predicted_15m_demand"),
                rs.getBigDecimal("predicted_24h_demand"),
                rs.getBigDecimal("days_of_cover"),
                rs.getString("stockout_risk"),
                rs.getInt("recommended_replenishment_quantity")
        ), limit);
    }

    public List<ControlTowerExceptionResponse> loadRecentExceptions(int limit) {
        String sql = """
                SELECT shipment_id, order_intent_id, awb_number, status_after_event AS current_status, scan_type AS last_scan_type, occurred_at, notes
                FROM scan_event.normalized_scan_events
                WHERE scan_type = 'EXCEPTION'
                ORDER BY occurred_at DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ControlTowerExceptionResponse(
                rs.getObject("shipment_id", UUID.class),
                rs.getObject("order_intent_id", UUID.class),
                rs.getString("awb_number"),
                rs.getString("current_status"),
                rs.getString("last_scan_type"),
                rs.getTimestamp("occurred_at").toInstant(),
                rs.getString("notes")
        ), limit);
    }

    private List<BucketCountResponse> countBy(String table, String column) {
        String sql = "SELECT " + column + " AS bucket_key, COUNT(*) AS bucket_count FROM " + table + " GROUP BY " + column + " ORDER BY bucket_count DESC, bucket_key";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new BucketCountResponse(
                rs.getString("bucket_key"),
                rs.getLong("bucket_count")
        ));
    }
}
