package com.craftedcharms.model;

/**
 * Enum representing the lifecycle status of an order.
 * Maps directly to the ENUM column in the orders table.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
