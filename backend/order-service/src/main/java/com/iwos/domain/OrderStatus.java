package com.iwos.domain;

/**
 * Order Status Enum
 *
 * Represents the lifecycle of an order in the system.
 *
 * Status Flow:
 * PENDING -> CONFIRMED -> PICKED -> PACKED -> SHIPPED -> DELIVERED
 *     |          |          |         |         |
 *     +----------+----------+---------+---------+-----> CANCELLED
 */
public enum OrderStatus {

    /**
     * Order created, awaiting confirmation
     */
    PENDING,

    /**
     * Order confirmed, ready for picking
     */
    CONFIRMED,

    /**
     * Items being picked from warehouse
     */
    PICKED,

    /**
     * Items packed and ready for shipment
     */
    PACKED,

    /**
     * Order shipped and in transit
     */
    SHIPPED,

    /**
     * Order delivered to customer
     */
    DELIVERED,

    /**
     * Order cancelled (can happen at any stage before delivery)
     */
    CANCELLED;

    /**
     * Check if order can be cancelled
     */
    public boolean canBeCancelled() {
        return this != DELIVERED && this != CANCELLED;
    }

    /**
     * Check if status transition is valid
     */
    public boolean canTransitionTo(OrderStatus newStatus) {
        if (newStatus == CANCELLED) {
            return canBeCancelled();
        }

        return switch (this) {
            case PENDING -> newStatus == CONFIRMED;
            case CONFIRMED -> newStatus == PICKED;
            case PICKED -> newStatus == PACKED;
            case PACKED -> newStatus == SHIPPED;
            case SHIPPED -> newStatus == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
