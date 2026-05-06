package com.iwos.forecasting.domain;

public class ForecastModelRunNotFoundException extends RuntimeException {

    public ForecastModelRunNotFoundException() {
        super("Forecast model run not found");
    }
}
