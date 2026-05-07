package com.iwos.controltower.domain;

public class ControlTowerNotFoundException extends RuntimeException {

    public ControlTowerNotFoundException() {
        super("No control tower snapshot found");
    }
}
