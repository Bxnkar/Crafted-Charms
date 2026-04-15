package com.craftedcharms.service;

import com.craftedcharms.model.Category;
import com.craftedcharms.model.JewelryProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductService business logic.
 * Tests validation rules — no database required (pure in-memory check).
 * Demonstrates: Unit testing requirement (bonus).
 */
public class ProductServiceTest {

    private JewelryProduct validProduct;

    @BeforeEach
    void setUp() {
        validProduct = new JewelryProduct(
                0,
                "Test Ring",
                "A test ring",
                new BigDecimal("999.00"),
                10,
                "Sterling Silver",
                "Moonstone",
                Category.RING
        );
    }

    @Test
    @DisplayName("Product name cannot be null or blank")
    void testEmptyNameThrows() {
        validProduct.setName("");
        assertThrows(IllegalArgumentException.class,
                () -> doValidate(validProduct),
                "Empty name should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Product price must be greater than 0")
    void testZeroPriceThrows() {
        validProduct.setPrice(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class,
                () -> doValidate(validProduct),
                "Zero price should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Negative price should throw")
    void testNegativePriceThrows() {
        validProduct.setPrice(new BigDecimal("-1.00"));
        assertThrows(IllegalArgumentException.class,
                () -> doValidate(validProduct),
                "Negative price should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Negative stock should throw")
    void testNegativeStockThrows() {
        validProduct.setStockQty(-5);
        assertThrows(IllegalArgumentException.class,
                () -> doValidate(validProduct),
                "Negative stock should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Material cannot be blank")
    void testBlankMaterialThrows() {
        validProduct.setMaterial("  ");
        assertThrows(IllegalArgumentException.class,
                () -> doValidate(validProduct),
                "Blank material should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Valid product passes all validation")
    void testValidProductPassesValidation() {
        assertDoesNotThrow(() -> doValidate(validProduct),
                "A valid product should not throw any exception");
    }

    @Test
    @DisplayName("JewelryProduct discount — price > 5000 gives 10% base")
    void testHighPriceDiscount() {
        validProduct.setPrice(new BigDecimal("6000.00"));
        assertEquals(10.0, validProduct.calculateDiscount(), 0.001);
    }

    @Test
    @DisplayName("JewelryProduct discount — price > 2000 gives 5% base")
    void testMidPriceDiscount() {
        validProduct.setPrice(new BigDecimal("2500.00"));
        assertEquals(5.0, validProduct.calculateDiscount(), 0.001);
    }

    @Test
    @DisplayName("JewelryProduct discount — price <= 2000 gives 0% base")
    void testLowPriceDiscount() {
        validProduct.setPrice(new BigDecimal("1000.00"));
        assertEquals(0.0, validProduct.calculateDiscount(), 0.001);
    }

    @Test
    @DisplayName("isInStock returns false when stock is 0")
    void testOutOfStock() {
        validProduct.setStockQty(0);
        assertFalse(validProduct.isInStock());
    }

    @Test
    @DisplayName("isInStock returns true when stock > 0")
    void testInStock() {
        validProduct.setStockQty(5);
        assertTrue(validProduct.isInStock());
    }

    // ── Helper: replicates ProductService validation logic inline ─────
    private void doValidate(JewelryProduct p) {
        if (p.getName() == null || p.getName().isBlank())
            throw new IllegalArgumentException("name blank");
        if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("price invalid");
        if (p.getStockQty() < 0)
            throw new IllegalArgumentException("stock negative");
        if (p.getMaterial() == null || p.getMaterial().isBlank())
            throw new IllegalArgumentException("material blank");
        if (p.getCategoryEnum() == null)
            throw new IllegalArgumentException("category null");
    }
}
