package com.craftedcharms.model;

import java.math.BigDecimal;

/**
 * Represents a Bracelet product.
 * Extends JewelryProduct — demonstrates deep Inheritance hierarchy.
 * Adds wrist-size attribute and overrides calculateDiscount().
 */
public class Bracelet extends JewelryProduct {

    private String wristSize;   // e.g. "S", "M", "L", "6.5 inches"

    public Bracelet() {}

    public Bracelet(int productId, String name, String description,
                    BigDecimal price, int stockQty,
                    String material, String gemstone, String wristSize) {
        super(productId, name, description, price, stockQty, material, gemstone, Category.BRACELET);
        this.wristSize = wristSize;
    }

    public String getWristSize()                  { return wristSize; }
    public void   setWristSize(String wristSize)  { this.wristSize = wristSize; }

    /** Bracelets earn an extra 1.5% discount on top of base. */
    @Override
    public double calculateDiscount() {
        return super.calculateDiscount() + 1.5;
    }

    @Override
    public String toString() {
        return super.toString() + " | Wrist: " + wristSize;
    }
}
