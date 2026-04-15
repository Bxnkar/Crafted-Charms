package com.craftedcharms.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer purchase order.
 * Aggregates OrderItems and tracks lifecycle status.
 * Demonstrates: Collections (ArrayList), Encapsulation.
 */
public class Order {

    private int orderId;
    private int customerId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String paymentMethod;
    private List<OrderItem> items;   // ArrayList — demonstrates use of Collections

    // Default constructor
    public Order() {
        this.items       = new ArrayList<>();
        this.orderDate   = LocalDateTime.now();
        this.status      = OrderStatus.PENDING;
        this.totalAmount = BigDecimal.ZERO;
    }

    // Parameterised constructor
    public Order(int orderId, int customerId, String shippingAddress, String paymentMethod) {
        this();
        this.orderId         = orderId;
        this.customerId      = customerId;
        this.shippingAddress = shippingAddress;
        this.paymentMethod   = paymentMethod;
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public int  getOrderId()                { return orderId; }
    public void setOrderId(int orderId)     { this.orderId = orderId; }

    public int  getCustomerId()                   { return customerId; }
    public void setCustomerId(int customerId)     { this.customerId = customerId; }

    public LocalDateTime getOrderDate()                { return orderDate; }
    public void          setOrderDate(LocalDateTime d) { this.orderDate = d; }

    public OrderStatus getStatus()               { return status; }
    public void        setStatus(OrderStatus s)  { this.status = s; }

    public BigDecimal getTotalAmount()               { return totalAmount; }
    public void       setTotalAmount(BigDecimal t)   { this.totalAmount = t; }

    public String getShippingAddress()                       { return shippingAddress; }
    public void   setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPaymentMethod()                     { return paymentMethod; }
    public void   setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public List<OrderItem> getItems()                    { return items; }
    public void            setItems(List<OrderItem> items) { this.items = items; }

    // ── Business helpers ───────────────────────────────────────────────

    public void addItem(OrderItem item) {
        this.items.add(item);
    }

    /** Recalculates totalAmount from current items list. */
    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String toString() {
        return String.format(
                "Order #%-5d | Customer: %-4d | Status: %-10s | Total: ₹%9.2f | Date: %s",
                orderId, customerId, status, totalAmount,
                orderDate != null ? orderDate.toLocalDate().toString() : "—");
    }
}
