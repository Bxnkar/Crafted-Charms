package com.craftedcharms.dao;

import com.craftedcharms.db.DatabaseConnection;
import com.craftedcharms.exception.DatabaseException;
import com.craftedcharms.model.Order;
import com.craftedcharms.model.OrderItem;
import com.craftedcharms.model.OrderStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of OrderDAO — MySQL backend.
 *
 * Fixes applied:
 *   ⚠3 — Replaced bare RuntimeException with DatabaseException.
 *   ⚠4 — ResultSet closed via nested try-with-resources in all read methods.
 */
public class OrderDAOImpl implements OrderDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────────────

    @Override
    public int placeOrder(Order order) {
        String sql = "INSERT INTO orders " +
                     "(customer_id, status, total_amount, shipping_address, payment_method) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getCustomerId());
            ps.setString(2, order.getStatus().name());
            ps.setBigDecimal(3, order.getTotalAmount());
            ps.setString(4, order.getShippingAddress());
            ps.setString(5, order.getPaymentMethod());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    order.setOrderId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("placeOrder", e.getMessage(), e);
        }
        return -1;
    }

    @Override
    public void addOrderItem(OrderItem item) {
        String sql = "INSERT INTO order_items " +
                     "(order_id, product_id, product_name, quantity, unit_price, subtotal) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getProductId());
            ps.setString(3, item.getProductName());
            ps.setInt(4, item.getQuantity());
            ps.setBigDecimal(5, item.getUnitPrice());
            ps.setBigDecimal(6, item.getSubtotal());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) item.setItemId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new DatabaseException("addOrderItem", e.getMessage(), e);
        }
    }

    // ── READ ──────────────────────────────────────────────────────────

    @Override
    public Optional<Order> getOrderById(int orderId) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order o = mapOrder(rs);
                    o.setItems(getOrderItems(orderId));
                    return Optional.of(o);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("getOrderById", e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_id DESC";
        try (Statement stmt = conn().createStatement();
             ResultSet  rs   = stmt.executeQuery(sql)) {         // both auto-closed
            while (rs.next()) list.add(mapOrder(rs));
        } catch (SQLException e) {
            throw new DatabaseException("getAllOrders", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Order> getOrdersByCustomerId(int customerId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE customer_id = ? ORDER BY order_id DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapOrder(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("getOrdersByCustomerId", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("getOrderItems", e.getMessage(), e);
        }
        return list;
    }

    // ── UPDATE ────────────────────────────────────────────────────────

    @Override
    public void updateOrderStatus(int orderId, OrderStatus status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("updateOrderStatus", e.getMessage(), e);
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────

    @Override
    public void cancelOrder(int orderId) {
        updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }

    // ── Private helpers ───────────────────────────────────────────────

    /** Maps a ResultSet row to an {@link Order}. */
    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setCustomerId(rs.getInt("customer_id"));
        o.setStatus(OrderStatus.valueOf(rs.getString("status")));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setShippingAddress(rs.getString("shipping_address"));
        o.setPaymentMethod(rs.getString("payment_method"));
        Timestamp ts = rs.getTimestamp("order_date");
        if (ts != null) o.setOrderDate(ts.toLocalDateTime());
        return o;
    }
}
