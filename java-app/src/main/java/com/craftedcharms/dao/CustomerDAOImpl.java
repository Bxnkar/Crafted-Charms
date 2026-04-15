package com.craftedcharms.dao;

import com.craftedcharms.db.DatabaseConnection;
import com.craftedcharms.exception.DatabaseException;
import com.craftedcharms.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of CustomerDAO — MySQL backend.
 *
 * Fixes applied:
 *   ⚠3 — Replaced bare RuntimeException with DatabaseException.
 *   ⚠4 — ResultSet closed via nested try-with-resources in all read methods.
 */
public class CustomerDAOImpl implements CustomerDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────────────

    @Override
    public void addCustomer(Customer c) {
        String sql = "INSERT INTO customers (user_id, full_name, email, phone, address, city) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getUserId());
            ps.setString(2, c.getFullName());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getPhone());
            ps.setString(5, c.getAddress());
            ps.setString(6, c.getCity());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new DatabaseException("addCustomer", e.getMessage(), e);
        }
    }

    // ── READ ──────────────────────────────────────────────────────────

    @Override
    public Optional<Customer> getCustomerById(int customerId) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("getCustomerById", e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Customer> getCustomerByUserId(int userId) {
        String sql = "SELECT * FROM customers WHERE user_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("getCustomerByUserId", e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY customer_id";
        try (Statement stmt = conn().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {      // both auto-closed
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new DatabaseException("getAllCustomers", e.getMessage(), e);
        }
        return list;
    }

    // ── UPDATE ────────────────────────────────────────────────────────

    @Override
    public void updateCustomer(Customer c) {
        String sql = "UPDATE customers SET full_name=?, email=?, phone=?, address=?, city=? " +
                     "WHERE customer_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, c.getFullName());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getAddress());
            ps.setString(5, c.getCity());
            ps.setInt(6, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("updateCustomer", e.getMessage(), e);
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────

    @Override
    public void deleteCustomer(int customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("deleteCustomer", e.getMessage(), e);
        }
    }

    // ── SEARCH ────────────────────────────────────────────────────────

    @Override
    public List<Customer> searchByName(String name) {
        return likeQuery("SELECT * FROM customers WHERE full_name LIKE ?", name);
    }

    @Override
    public List<Customer> searchByCity(String city) {
        return likeQuery("SELECT * FROM customers WHERE city LIKE ?", city);
    }

    // ── Private helpers ───────────────────────────────────────────────

    /** LIKE search helper — ResultSet closed via nested try-with-resources. */
    private List<Customer> likeQuery(String sql, String keyword) {
        List<Customer> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("likeQuery", e.getMessage(), e);
        }
        return list;
    }

    /** Maps a ResultSet row to a {@link Customer} object. */
    private Customer map(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("customer_id"));
        c.setUserId(rs.getInt("user_id"));
        c.setFullName(rs.getString("full_name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        c.setCity(rs.getString("city"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
        return c;
    }
}
