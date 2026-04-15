package com.craftedcharms.model;

import java.math.BigDecimal;

/**
 * Represents a Ring product.
 * Extends JewelryProduct — demonstrates deep Inheritance hierarchy.
 * Adds ring-size attribute and overrides calculateDiscount().
 */
public class Ring extends JewelryProduct {

    private String ringSize;   // e.g. "US 7", "6", "Free Size"

    public Ring() {}

    public Ring(int productId, String name, String description,
                BigDecimal price, int stockQty,
                String material, String gemstone, String ringSize) {
        super(productId, name, description, price, stockQty, material, gemstone, Category.RING);
        this.ringSize = ringSize;
    }

    public String getRingSize()               { return ringSize; }
    public void   setRingSize(String size)    { this.ringSize = size; }

    /** Rings earn an extra 2% discount on top of base. */
    @Override
    public double calculateDiscount() {
        return super.calculateDiscount() + 2.0;
    }

    @Override
    public String toString() {
        return super.toString() + " | Size: " + ringSize;
    }
}
