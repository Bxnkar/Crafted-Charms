package com.craftedcharms.model;

import java.math.BigDecimal;

/**
 * Represents a single line item within an Order.
 * Stores a snapshot of the product name and price at time of purchase.
 * Demonstrates: Encapsulation.
 */
public class OrderItem {

    private int itemId;
    private int orderId;
    private int productId;
    private String productName;   // snapshot — name at time of purchase
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    // Default constructor
    public OrderItem() {}

    // Parameterised constructor
    public OrderItem(int orderId, int productId, String productName,
                     int quantity, BigDecimal unitPrice) {
        this.orderId     = orderId;
        this.productId   = productId;
        this.productName = productName;
        this.quantity    = quantity;
        this.unitPrice   = unitPrice;
        this.subtotal    = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public int  getItemId()               { return itemId; }
    public void setItemId(int itemId)     { this.itemId = itemId; }

    public int  getOrderId()              { return orderId; }
    public void setOrderId(int orderId)   { this.orderId = orderId; }

    public int  getProductId()                  { return productId; }
    public void setProductId(int productId)     { this.productId = productId; }

    public String getProductName()                    { return productName; }
    public void   setProductName(String productName)  { this.productName = productName; }

    public int  getQuantity()               { return quantity; }
    public void setQuantity(int quantity)   { this.quantity = quantity; }

    public BigDecimal getUnitPrice()             { return unitPrice; }
    public void       setUnitPrice(BigDecimal p) { this.unitPrice = p; }

    public BigDecimal getSubtotal()             { return subtotal; }
    public void       setSubtotal(BigDecimal s) { this.subtotal = s; }

    @Override
    public String toString() {
        return String.format("  %-35s | Qty: %3d | ₹%8.2f each | Subtotal: ₹%9.2f",
                productName, quantity, unitPrice, subtotal);
    }
}
