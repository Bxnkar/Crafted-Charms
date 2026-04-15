package com.craftedcharms.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Abstract base class for all products sold on Crafted Charms.
 * Demonstrates: Abstract Class, Encapsulation, Method overriding.
 * All product-type subclasses must implement getCategory() and calculateDiscount().
 */
public abstract class Product {

    private int productId;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQty;
    private boolean isActive;
    private LocalDateTime createdAt;

    // Default constructor
    public Product() {}

    // Parameterised constructor
    public Product(int productId, String name, String description,
                   BigDecimal price, int stockQty) {
        this.productId   = productId;
        this.name        = name;
        this.description = description;
        this.price       = price;
        this.stockQty    = stockQty;
        this.isActive    = true;
        this.createdAt   = LocalDateTime.now();
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public int  getProductId()               { return productId; }
    public void setProductId(int productId)  { this.productId = productId; }

    public String getName()             { return name; }
    public void   setName(String name)  { this.name = name; }

    public String getDescription()                    { return description; }
    public void   setDescription(String description)  { this.description = description; }

    public BigDecimal getPrice()             { return price; }
    public void       setPrice(BigDecimal p) { this.price = p; }

    public int  getStockQty()               { return stockQty; }
    public void setStockQty(int stockQty)   { this.stockQty = stockQty; }

    public boolean isActive()                  { return isActive; }
    public void    setActive(boolean active)   { this.isActive = active; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void          setCreatedAt(LocalDateTime t)  { this.createdAt = t; }

    // ── Abstract methods — forces subclasses to implement ──────────────

    /** Returns the product's category string (e.g. "RING", "NECKLACE"). */
    public abstract String getCategory();

    /**
     * Returns the discount percentage applicable to this product.
     * Demonstrates: Polymorphism via abstract method.
     */
    public abstract double calculateDiscount();

    // ── Concrete helper methods ────────────────────────────────────────

    public boolean isInStock() {
        return stockQty > 0;
    }

    public BigDecimal getDiscountedPrice() {
        double discount = calculateDiscount();
        return price.multiply(BigDecimal.valueOf(1.0 - discount / 100.0));
    }

    @Override
    public String toString() {
        return String.format("[%4d] %-35s | %9s | ₹%8.2f | Stock: %3d",
                productId, name, getCategory(), price, stockQty);
    }
}
