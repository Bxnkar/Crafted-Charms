package com.craftedcharms.dao;

import com.craftedcharms.model.Order;
import com.craftedcharms.model.OrderItem;
import com.craftedcharms.model.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * DAO Interface for Order and OrderItem CRUD operations.
 * Demonstrates: Interface usage (OOP requirement).
 * Implemented by OrderDAOImpl.
 */
public interface OrderDAO {

    /** Insert a new order; returns the generated orderId. */
    int placeOrder(Order order);

    /** Insert a single order line item. */
    void addOrderItem(OrderItem item);

    /** Fetch an order by ID (includes its items). */
    Optional<Order> getOrderById(int orderId);

    /** Fetch all orders (admin view). */
    List<Order> getAllOrders();

    /** Fetch orders for a specific customer. */
    List<Order> getOrdersByCustomerId(int customerId);

    /** Update the status field of an order. */
    void updateOrderStatus(int orderId, OrderStatus status);

    /** Fetch all line items for a given order. */
    List<OrderItem> getOrderItems(int orderId);

    /** Convenience: set status to CANCELLED. */
    void cancelOrder(int orderId);
}
