package com.iwos.forecasting.domain;

import java.util.UUID;

public class ForecastNotFoundException extends RuntimeException {

    public ForecastNotFoundException(UUID forecastId) {
        super("Forecast not found: " + forecastId);
    }
}
