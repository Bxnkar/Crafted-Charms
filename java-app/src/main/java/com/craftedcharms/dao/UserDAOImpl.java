package com.craftedcharms.dao;

import com.craftedcharms.db.DatabaseConnection;
import com.craftedcharms.exception.DatabaseException;
import com.craftedcharms.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of UserDAO — MySQL backend.
 *
 * Fixes applied:
 *   ⚠3 — Replaced bare RuntimeException with DatabaseException.
 *   ⚠4 — ResultSet closed via nested try-with-resources in all read methods.
 *
 * Polymorphism highlight: map() instantiates AdminUser or CustomerUser
 * based on the role column — correct subtype returned at runtime.
 */
public class UserDAOImpl implements UserDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────────────

    @Override
    public void addUser(User user) {
        String sql = "INSERT INTO users (username, password_hash, role, full_name, email) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new DatabaseException("addUser", e.getMessage(), e);
        }
    }

    // ── READ ──────────────────────────────────────────────────────────

    @Override
    public Optional<User> getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("getUserById", e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("getUserByUsername", e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id";
        try (Statement stmt = conn().createStatement();
             ResultSet  rs   = stmt.executeQuery(sql)) {     // both auto-closed
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new DatabaseException("getAllUsers", e.getMessage(), e);
        }
        return list;
    }

    // ── UPDATE ────────────────────────────────────────────────────────

    @Override
    public void updateUser(User user) {
        String sql = "UPDATE users SET full_name=?, email=?, role=?, password_hash=? " +
                     "WHERE user_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole().name());
            ps.setString(4, user.getPasswordHash());
            ps.setInt(5, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("updateUser", e.getMessage(), e);
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────

    @Override
    public void deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("deleteUser", e.getMessage(), e);
        }
    }

    // ── EXISTENCE CHECKS ──────────────────────────────────────────────

    @Override
    public boolean usernameExists(String username) {
        return exists("SELECT COUNT(*) FROM users WHERE username = ?", username);
    }

    @Override
    public boolean emailExists(String email) {
        return exists("SELECT COUNT(*) FROM users WHERE email = ?", email);
    }

    // ── Private helpers ───────────────────────────────────────────────

    /** Single-parameter COUNT existence check — ResultSet properly closed. */
    private boolean exists(String sql, String value) {
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("exists", e.getMessage(), e);
        }
    }

    /**
     * Maps a ResultSet row to the correct User subtype based on role column.
     * Demonstrates: Polymorphism — AdminUser or CustomerUser materialised at runtime.
     */
    private User map(ResultSet rs) throws SQLException {
        Role role = Role.valueOf(rs.getString("role"));
        User user = (role == Role.ADMIN) ? new AdminUser() : new CustomerUser();
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(role);
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) user.setCreatedAt(ts.toLocalDateTime());
        return user;
    }
}
