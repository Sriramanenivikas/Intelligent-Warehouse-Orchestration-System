package com.iwos.forecasting.domain;

import java.math.BigDecimal;

public record ForecastDemandSnapshot(
        String nodeId,
        String sku,
        int currentOnHandQuantity,
        int currentReservedQuantity,
        BigDecimal demandLast1h,
        BigDecimal demandLast6h,
        BigDecimal demandLast24h
) {
}
