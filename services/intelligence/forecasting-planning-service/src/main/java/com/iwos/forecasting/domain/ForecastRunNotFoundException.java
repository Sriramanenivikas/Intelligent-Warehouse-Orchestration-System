package com.iwos.forecasting.domain;

public class ForecastRunNotFoundException extends RuntimeException {

    public ForecastRunNotFoundException() {
        super("No forecast run found");
    }
}
