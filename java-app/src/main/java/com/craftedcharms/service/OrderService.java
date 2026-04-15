package com.craftedcharms.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.craftedcharms.dao.OrderDAO;
import com.craftedcharms.dao.OrderDAOImpl;
import com.craftedcharms.dao.ProductDAO;
import com.craftedcharms.dao.ProductDAOImpl;
import com.craftedcharms.db.DatabaseConnection;
import com.craftedcharms.model.CartItem;
import com.craftedcharms.model.JewelryProduct;
import com.craftedcharms.model.Order;
import com.craftedcharms.model.OrderItem;
import com.craftedcharms.model.OrderStatus;

/**
 * Business Logic Layer — Shopping cart and order management.
 *
 * Cart is managed in-memory using a HashMap<sessionId, List<CartItem>>.
 * Demonstrates: Collections (ArrayList, HashMap), Business logic coordination.
 */
public class OrderService {

    private final OrderDAO   orderDAO;
    private final ProductDAO productDAO;

    /**
     * In-memory session carts: sessionId → list of cart items.
     * Demonstrates: Use of HashMap collection.
     */
    private final Map<String, List<CartItem>> sessionCarts;

    public OrderService() {
        this.orderDAO     = new OrderDAOImpl();
        this.productDAO   = new ProductDAOImpl();
        this.sessionCarts = new HashMap<>();
    }

    // ── Cart Operations ───────────────────────────────────────────────

    /** Add a product to the in-memory cart; merges quantities if already present. */
    public void addToCart(String sessionId, JewelryProduct product, int quantity) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be at least 1.");
        if (!product.isInStock())
            throw new IllegalArgumentException("Product is out of stock.");
        if (quantity > product.getStockQty())
            throw new IllegalArgumentException("Only " + product.getStockQty() + " units available.");

        List<CartItem> cart = sessionCarts.computeIfAbsent(sessionId, k -> new ArrayList<>());

        boolean merged = false;
        for (CartItem item : cart) {
            if (item.getProduct().getProductId() == product.getProductId()) {
                item.setQuantity(item.getQuantity() + quantity);
                merged = true;
                break;
            }
        }
        if (!merged) cart.add(new CartItem(product, quantity));
    }

    public void removeFromCart(String sessionId, int productId) {
        List<CartItem> cart = getCart(sessionId);
        cart.removeIf(ci -> ci.getProduct().getProductId() == productId);
    }

    public List<CartItem> getCart(String sessionId) {
        return sessionCarts.getOrDefault(sessionId, new ArrayList<>());
    }

    public BigDecimal getCartTotal(String sessionId) {
        return getCart(sessionId).stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void clearCart(String sessionId) {
        sessionCarts.remove(sessionId);
    }

    // ── Order Placement ───────────────────────────────────────────────

    /**
     * Converts the in-memory cart into a persisted Order.
     * Also decrements product stock quantities.
     */
    public Order placeOrder(String sessionId, int customerId,
                            String shippingAddress, String paymentMethod) {
        List<CartItem> cart = getCart(sessionId);
        if (cart.isEmpty())
            throw new IllegalStateException("Cannot place order — cart is empty.");

        BigDecimal total = getCartTotal(sessionId);
        Order order = new Order(0, customerId, shippingAddress, paymentMethod);
        order.setTotalAmount(total);

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);

            int orderId = orderDAO.placeOrder(order);

            for (CartItem ci : cart) {
                // Re-validate stock at checkout time before persisting.
                JewelryProduct latest = productDAO.getProductById(ci.getProduct().getProductId())
                        .orElseThrow(() -> new IllegalStateException("Product no longer exists."));
                if (ci.getQuantity() > latest.getStockQty()) {
                    throw new IllegalStateException("Insufficient stock for: " + latest.getName());
                }

                OrderItem item = new OrderItem(
                        orderId,
                        latest.getProductId(),
                        latest.getName(),
                        ci.getQuantity(),
                        latest.getPrice());
                orderDAO.addOrderItem(item);

                int remaining = latest.getStockQty() - ci.getQuantity();
                productDAO.updateStock(latest.getProductId(), remaining);
            }

            conn.commit();
            clearCart(sessionId);
            return order;
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                throw new IllegalStateException("Order failed and rollback also failed.", rollbackEx);
            }
            throw new IllegalStateException("Unable to place order: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
                // Keep application running; next DB operation will surface errors if any.
            }
        }
    }

    // ── Order Retrieval ───────────────────────────────────────────────

    public Optional<Order> getOrder(int orderId) {
        return orderDAO.getOrderById(orderId);
    }

    public List<Order> getAllOrders() {
        return orderDAO.getAllOrders();
    }

    public List<Order> getCustomerOrders(int customerId) {
        return orderDAO.getOrdersByCustomerId(customerId);
    }

    public List<OrderItem> getOrderItems(int orderId) {
        return orderDAO.getOrderItems(orderId);
    }

    // ── Order Management ──────────────────────────────────────────────

    public void updateStatus(int orderId, OrderStatus status) {
        orderDAO.updateOrderStatus(orderId, status);
    }

    public void cancelOrder(int orderId) {
        orderDAO.cancelOrder(orderId);
    }
}
