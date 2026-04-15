package com.craftedcharms.model;

import java.math.BigDecimal;

/**
 * Represents a Necklace product.
 * Extends JewelryProduct — demonstrates deep Inheritance hierarchy.
 * Adds chain length attribute and overrides calculateDiscount().
 */
public class Necklace extends JewelryProduct {

    private String length;   // e.g. "16 inches", "18 inches", "Adjustable"

    public Necklace() {}

    public Necklace(int productId, String name, String description,
                    BigDecimal price, int stockQty,
                    String material, String gemstone, String length) {
        super(productId, name, description, price, stockQty, material, gemstone, Category.NECKLACE);
        this.length = length;
    }

    public String getLength()               { return length; }
    public void   setLength(String length)  { this.length = length; }

    /** Necklaces earn an extra 3% discount on top of base. */
    @Override
    public double calculateDiscount() {
        return super.calculateDiscount() + 3.0;
    }

    @Override
    public String toString() {
        return super.toString() + " | Chain: " + length;
    }
}
