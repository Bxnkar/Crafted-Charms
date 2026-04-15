package com.craftedcharms.service;

import com.craftedcharms.dao.ProductDAO;
import com.craftedcharms.dao.ProductDAOImpl;
import com.craftedcharms.model.JewelryProduct;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Business Logic Layer — Product management.
 * Wraps ProductDAO with validation and business rules.
 *
 * Demonstrates: Method Overloading (three search() variants).
 */
public class ProductService {

    private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAOImpl();
    }

    // ── CREATE ────────────────────────────────────────────────────────

    public void addProduct(JewelryProduct product) {
        validate(product);
        productDAO.addProduct(product);
    }

    // ── READ ──────────────────────────────────────────────────────────

    public Optional<JewelryProduct> getProduct(int productId) {
        return productDAO.getProductById(productId);
    }

    public List<JewelryProduct> getAllProducts() {
        return productDAO.getAllProducts();
    }

    public List<JewelryProduct> getAvailableProducts() {
        return productDAO.getActiveProducts();
    }

    // ── UPDATE ────────────────────────────────────────────────────────

    public void updateProduct(JewelryProduct product) {
        validate(product);
        ensureExists(product.getProductId());
        productDAO.updateProduct(product);
    }

    public void updateStock(int productId, int newStock) {
        if (newStock < 0)
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        ensureExists(productId);
        productDAO.updateStock(productId, newStock);
    }

    // ── DELETE ────────────────────────────────────────────────────────

    public void removeProduct(int productId) {
        ensureExists(productId);
        productDAO.deleteProduct(productId);
    }

    // ── SEARCH — Overloaded methods ───────────────────────────────────
    // Demonstrates: Method Overloading (same name, different parameter lists)

    /** Search by name only. */
    public List<JewelryProduct> search(String name) {
        return productDAO.searchByName(name);
    }

    /** Search by name AND category. Pass "" to skip either filter. */
    public List<JewelryProduct> search(String name, String category) {
        boolean hasName = name != null && !name.isBlank();
        boolean hasCat  = category != null && !category.isBlank();
        if (hasName && hasCat) return productDAO.searchByNameAndCategory(name, category);
        if (hasName)           return productDAO.searchByName(name);
        if (hasCat)            return productDAO.searchByCategory(category);
        return productDAO.getActiveProducts();
    }

    /** Search by name, category, AND maximum price. */
    public List<JewelryProduct> search(String name, String category, BigDecimal maxPrice) {
        if (maxPrice == null) return search(name, category);
        boolean hasName = name != null && !name.isBlank();
        boolean hasCat  = category != null && !category.isBlank();
        if (hasName && hasCat) return productDAO.searchByNameCategoryPrice(name, category, maxPrice);
        // fallback: filter in memory
        List<JewelryProduct> base = search(name, category);
        base.removeIf(p -> p.getPrice().compareTo(maxPrice) > 0);
        return base;
    }

    // ── Validation ────────────────────────────────────────────────────

    private void validate(JewelryProduct p) {
        if (p.getName() == null || p.getName().isBlank())
            throw new IllegalArgumentException("Product name cannot be empty.");
        if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Product price must be greater than 0.");
        if (p.getStockQty() < 0)
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        if (p.getMaterial() == null || p.getMaterial().isBlank())
            throw new IllegalArgumentException("Material cannot be empty.");
        if (p.getCategoryEnum() == null)
            throw new IllegalArgumentException("Category must be specified.");
    }

    private void ensureExists(int productId) {
        if (productDAO.getProductById(productId).isEmpty())
            throw new IllegalArgumentException("Product with ID " + productId + " not found.");
    }
}
