package com.craftedcharms.dao;

import com.craftedcharms.model.JewelryProduct;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * DAO Interface for Product CRUD operations.
 * Demonstrates: Interface usage (OOP requirement).
 * Implemented by ProductDAOImpl.
 */
public interface ProductDAO {

    /** Insert a new product; sets the generated productId back on the object. */
    void addProduct(JewelryProduct product);

    /** Fetch a product by its ID. */
    Optional<JewelryProduct> getProductById(int productId);

    /** Fetch all products (active + inactive). */
    List<JewelryProduct> getAllProducts();

    /** Fetch only is_active = true products. */
    List<JewelryProduct> getActiveProducts();

    /** Update all mutable fields of an existing product. */
    void updateProduct(JewelryProduct product);

    /** Soft-delete: sets is_active = false. */
    void deleteProduct(int productId);

    /** Case-insensitive LIKE search by product name. */
    List<JewelryProduct> searchByName(String name);

    /** Exact category search (RING, NECKLACE, …). */
    List<JewelryProduct> searchByCategory(String category);

    /** Combined name + category search. */
    List<JewelryProduct> searchByNameAndCategory(String name, String category);

    /** Search by name + category + max price. Demonstrates method overloading concept at service layer. */
    List<JewelryProduct> searchByNameCategoryPrice(String name, String category, BigDecimal maxPrice);

    /** Directly update stock quantity for a product. */
    void updateStock(int productId, int newStock);
}
