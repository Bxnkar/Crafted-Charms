package com.craftedcharms.dao;

import com.craftedcharms.db.DatabaseConnection;
import com.craftedcharms.exception.DatabaseException;
import com.craftedcharms.model.Category;
import com.craftedcharms.model.JewelryProduct;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of ProductDAO — MySQL backend.
 *
 * Fixes applied:
 *   ⚠3 — Replaced bare RuntimeException with DatabaseException.
 *   ⚠4 — Fixed ResultSet resource leak: query() now uses proper
 *          nested try-with-resources so ResultSet is always closed.
 *   ⚠5 — Persists subclass-specific attributes (ring_size,
 *          necklace_length, wrist_size) via the extra_attribute column.
 */
public class ProductDAOImpl implements ProductDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────────────

    @Override
    public void addProduct(JewelryProduct p) {
        String sql = "INSERT INTO products " +
                     "(name, description, category, material, gemstone, " +
                     " price, stock_qty, is_active, extra_attribute) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getCategory());
            ps.setString(4, p.getMaterial());
            ps.setString(5, p.getGemstone());
            ps.setBigDecimal(6, p.getPrice());
            ps.setInt(7, p.getStockQty());
            ps.setBoolean(8, p.isActive());
            ps.setString(9, p.getExtraAttribute());   // ring_size / length / wrist_size
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setProductId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new DatabaseException("addProduct", e.getMessage(), e);
        }
    }

    // ── READ ──────────────────────────────────────────────────────────

    @Override
    public Optional<JewelryProduct> getProductById(int productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("getProductById", e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<JewelryProduct> getAllProducts() {
        return query("SELECT * FROM products ORDER BY product_id");
    }

    @Override
    public List<JewelryProduct> getActiveProducts() {
        return query("SELECT * FROM products WHERE is_active = 1 ORDER BY category, name");
    }

    // ── UPDATE ────────────────────────────────────────────────────────

    @Override
    public void updateProduct(JewelryProduct p) {
        String sql = "UPDATE products " +
                     "SET name=?, description=?, category=?, material=?, gemstone=?, " +
                     "    price=?, stock_qty=?, is_active=?, extra_attribute=? " +
                     "WHERE product_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getCategory());
            ps.setString(4, p.getMaterial());
            ps.setString(5, p.getGemstone());
            ps.setBigDecimal(6, p.getPrice());
            ps.setInt(7, p.getStockQty());
            ps.setBoolean(8, p.isActive());
            ps.setString(9, p.getExtraAttribute());
            ps.setInt(10, p.getProductId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("updateProduct", e.getMessage(), e);
        }
    }

    @Override
    public void updateStock(int productId, int newStock) {
        String sql = "UPDATE products SET stock_qty = ? WHERE product_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, newStock);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("updateStock", e.getMessage(), e);
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────

    @Override
    public void deleteProduct(int productId) {
        // Soft delete — preserves order history
        String sql = "UPDATE products SET is_active = 0 WHERE product_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("deleteProduct", e.getMessage(), e);
        }
    }

    // ── SEARCH ────────────────────────────────────────────────────────

    @Override
    public List<JewelryProduct> searchByName(String name) {
        return paramQuery(
                "SELECT * FROM products WHERE name LIKE ? AND is_active=1",
                "%" + name + "%");
    }

    @Override
    public List<JewelryProduct> searchByCategory(String category) {
        return paramQuery(
                "SELECT * FROM products WHERE category=? AND is_active=1",
                category.toUpperCase());
    }

    @Override
    public List<JewelryProduct> searchByNameAndCategory(String name, String category) {
        String sql = "SELECT * FROM products WHERE name LIKE ? AND category=? AND is_active=1";
        List<JewelryProduct> results = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            ps.setString(2, category.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(map(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("searchByNameAndCategory", e.getMessage(), e);
        }
        return results;
    }

    @Override
    public List<JewelryProduct> searchByNameCategoryPrice(String name, String category,
                                                           BigDecimal maxPrice) {
        String sql = "SELECT * FROM products " +
                     "WHERE name LIKE ? AND category=? AND price<=? AND is_active=1";
        List<JewelryProduct> results = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            ps.setString(2, category.toUpperCase());
            ps.setBigDecimal(3, maxPrice);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(map(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("searchByNameCategoryPrice", e.getMessage(), e);
        }
        return results;
    }

    // ── Private helpers ───────────────────────────────────────────────

    /**
     * Executes a no-parameter SELECT and returns a list of products.
     *
     * Fix ⚠4: Both Statement AND ResultSet are in the try-with-resources
     * header so both are closed even if an exception occurs.
     */
    private List<JewelryProduct> query(String sql) {
        List<JewelryProduct> list = new ArrayList<>();
        try (Statement stmt = conn().createStatement();
             ResultSet  rs   = stmt.executeQuery(sql)) {   // ← rs now auto-closed
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new DatabaseException("query", e.getMessage(), e);
        }
        return list;
    }

    /**
     * Executes a single-parameter LIKE/equality SELECT query.
     * ResultSet is closed via nested try-with-resources.
     */
    private List<JewelryProduct> paramQuery(String sql, String param) {
        List<JewelryProduct> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("paramQuery", e.getMessage(), e);
        }
        return list;
    }

    /**
     * Maps a ResultSet row to a {@link JewelryProduct}.
     *
     * Fix ⚠5: Also reads extra_attribute column which stores the
     * category-specific attribute (ring size / chain length / wrist size).
     */
    private JewelryProduct map(ResultSet rs) throws SQLException {
        JewelryProduct p = new JewelryProduct();
        p.setProductId(rs.getInt("product_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        try {
            p.setCategoryEnum(Category.valueOf(rs.getString("category")));
        } catch (IllegalArgumentException e) {
            p.setCategoryEnum(Category.RING);   // safe fallback
        }
        p.setMaterial(rs.getString("material"));
        p.setGemstone(rs.getString("gemstone"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setActive(rs.getBoolean("is_active"));
        p.setExtraAttribute(rs.getString("extra_attribute")); // ring_size / length / wrist
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) p.setCreatedAt(ts.toLocalDateTime());
        return p;
    }
}
