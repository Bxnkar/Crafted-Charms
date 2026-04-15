package com.craftedcharms.model;

import java.math.BigDecimal;

/**
 * Represents a transient in-memory cart item.
 * Holds a product reference and quantity — NOT persisted to DB.
 * Demonstrates: Encapsulation, use of Product polymorphism.
 */
public class CartItem {

    private Product product;   // any Product subtype — demonstrates Polymorphism
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product  = product;
        this.quantity = quantity;
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public Product getProduct()               { return product; }
    public void    setProduct(Product product) { this.product = product; }

    public int  getQuantity()               { return quantity; }
    public void setQuantity(int quantity)   { this.quantity = quantity; }

    // ── Business helpers ───────────────────────────────────────────────

    public BigDecimal getSubtotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public void incrementQuantity() { this.quantity++; }
    public void decrementQuantity() { if (this.quantity > 0) this.quantity--; }

    @Override
    public String toString() {
        return String.format("%-35s | Qty: %3d | ₹%8.2f | Subtotal: ₹%9.2f",
                product.getName(), quantity, product.getPrice(), getSubtotal());
    }
}
