package com.craftedcharms.model;

import java.math.BigDecimal;

/**
 * Concrete product class representing a jewelry item.
 * Extends Product — demonstrates Inheritance and Method overriding.
 * Stores material, gemstone, and category specifics.
 */
public class JewelryProduct extends Product {

    private String material;        // e.g. "Sterling Silver", "Gold Plated"
    private String gemstone;        // e.g. "Moonstone", "Amethyst", or null
    private Category category;

    /**
     * Stores the category-specific attribute for DB persistence.
     * Mapped to the extra_attribute column in MySQL products table.
     *   RING     → ring size  (e.g. "US 7")
     *   NECKLACE → chain length (e.g. "18 inches")
     *   BRACELET → wrist size (e.g. "M")
     *   EARRING  → null
     *
     * Fix ⚠5: Subclass attributes are now persisted to MySQL.
     */
    private String extraAttribute;

    // Default constructor
    public JewelryProduct() {}

    // Parameterised constructor
    public JewelryProduct(int productId, String name, String description,
                          BigDecimal price, int stockQty,
                          String material, String gemstone, Category category) {
        super(productId, name, description, price, stockQty);
        this.material = material;
        this.gemstone = gemstone;
        this.category = category;
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public String   getMaterial()                  { return material; }
    public void     setMaterial(String material)   { this.material = material; }

    public String   getGemstone()                  { return gemstone; }
    public void     setGemstone(String gemstone)   { this.gemstone = gemstone; }

    public Category getCategoryEnum()                   { return category; }
    public void     setCategoryEnum(Category category)  { this.category = category; }

    public String getExtraAttribute()                        { return extraAttribute; }
    public void   setExtraAttribute(String extraAttribute)   { this.extraAttribute = extraAttribute; }

    // ── Abstract method implementations ───────────────────────────────

    @Override
    public String getCategory() {
        return category != null ? category.name() : "JEWELRY";
    }

    /**
     * Base discount logic — higher price → higher discount.
     * Subclasses override this to add category-specific bonuses.
     * Demonstrates: Method overriding (Polymorphism).
     */
    @Override
    public double calculateDiscount() {
        if (getPrice().compareTo(BigDecimal.valueOf(5000)) > 0) return 10.0;
        if (getPrice().compareTo(BigDecimal.valueOf(2000)) > 0) return 5.0;
        return 0.0;
    }

    @Override
    public String toString() {
        String gem = (gemstone == null || gemstone.isBlank() || gemstone.equalsIgnoreCase("none"))
                ? "—" : gemstone;
        return super.toString() + String.format(" | %-18s | %s", material, gem);
    }
}
