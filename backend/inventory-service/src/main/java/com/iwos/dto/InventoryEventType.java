package com.iwos.dto;

/**
 * Inventory Event Types for Kafka
 */
public enum InventoryEventType {
    INVENTORY_RESERVED,
    INVENTORY_RELEASED,
    INVENTORY_INSUFFICIENT,
    INVENTORY_ADJUSTED
}
