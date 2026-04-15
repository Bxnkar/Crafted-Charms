package com.craftedcharms.ui;

import com.craftedcharms.model.*;
import com.craftedcharms.service.*;
import com.craftedcharms.util.ConsoleColors;
import com.craftedcharms.util.InputValidator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Presentation Layer — Customer Portal.
 * Lets authenticated customers browse products, manage cart, place orders,
 * view order history, and update their own profile.
 */
public class CustomerMenu {

    private final AuthService     authService;
    private final ProductService  productService;
    private final OrderService    orderService;
    private final CustomerService customerService;
    private final Scanner sc;

    private final String sessionId;
    private Customer customerProfile;

    public CustomerMenu(AuthService authService, Scanner sc) {
        this.authService     = authService;
        this.productService  = new ProductService();
        this.orderService    = new OrderService();
        this.customerService = new CustomerService();
        this.sc              = sc;
        this.sessionId       = "session_" + authService.getCurrentUser().getId();
    }

    public void show() {
        // Load profile linked to the logged-in user account
        customerService.getCustomerByUserId(authService.getCurrentUser().getId())
                .ifPresent(c -> this.customerProfile = c);

        while (true) {
            ConsoleColors.header("CUSTOMER PORTAL  ·  " + authService.getCurrentUser().getFullName());
            System.out.println("    1. Browse Jewelry");
            System.out.println("    2. Search Products");
            System.out.println("    3. View Cart");
            System.out.println("    4. Checkout");
            System.out.println("    5. My Orders");
            System.out.println("    6. My Profile");
            System.out.println("    0. Logout");
            System.out.print("\n  → ");
            switch (sc.nextLine().trim()) {
                case "1" -> browseProducts();
                case "2" -> searchProducts();
                case "3" -> viewCart();
                case "4" -> checkout();
                case "5" -> myOrders();
                case "6" -> myProfile();
                case "0" -> { authService.logout(); ConsoleColors.info("Logged out. See you again! 🪷"); return; }
                default  -> ConsoleColors.warning("Invalid option.");
            }
        }
    }

    // ── Browse ────────────────────────────────────────────────────────

    private void browseProducts() {
        List<JewelryProduct> products = productService.getAvailableProducts();
        if (products.isEmpty()) { ConsoleColors.info("No products available right now."); return; }

        ConsoleColors.header("✨ CRAFTED CHARMS JEWELLERY  (" + products.size() + " items)");
        System.out.printf("%n  %-4s %-35s %-10s %9s  %s%n",
                "ID", "Name", "Category", "Price(₹)", "Status");
        ConsoleColors.divider();
        for (JewelryProduct p : products) {
            System.out.printf("  %-4d %-35s %-10s %9.2f  %s%n",
                    p.getProductId(), p.getName(), p.getCategory(),
                    p.getPrice(), p.isInStock() ? "✓ In Stock" : "✗ Out of Stock");
        }

        System.out.print("\n  Enter Product ID to add to cart (0 = back): ");
        String in = sc.nextLine().trim();
        if (!in.equals("0")) {
            try { addToCartFlow(Integer.parseInt(in)); }
            catch (NumberFormatException e) { ConsoleColors.warning("Invalid ID."); }
        }
    }

    private void searchProducts() {
        ConsoleColors.header("SEARCH PRODUCTS");
        String keyword = InputValidator.readNonEmpty(sc, "  Keyword: ");
        List<JewelryProduct> results = productService.search(keyword);
        if (results.isEmpty()) { ConsoleColors.info("No products matched '" + keyword + "'."); return; }

        results.forEach(p ->
                System.out.printf("  [%d] %-35s ₹%8.2f  %s%n",
                        p.getProductId(), p.getName(), p.getPrice(),
                        p.isInStock() ? "(In Stock)" : "(Out of Stock)"));

        System.out.print("\n  Add to cart — Product ID (0 = skip): ");
        String in = sc.nextLine().trim();
        if (!in.equals("0")) {
            try { addToCartFlow(Integer.parseInt(in)); }
            catch (NumberFormatException e) { ConsoleColors.warning("Invalid ID."); }
        }
    }

    private void addToCartFlow(int productId) {
        Optional<JewelryProduct> opt = productService.getProduct(productId);
        if (opt.isEmpty()) { ConsoleColors.error("Product not found."); return; }
        JewelryProduct p = opt.get();
        if (!p.isInStock()) { ConsoleColors.warning("Sorry, '" + p.getName() + "' is out of stock."); return; }

        int qty = InputValidator.readInt(sc,
                "  Quantity (max " + p.getStockQty() + "): ", 1, p.getStockQty());
        try {
            orderService.addToCart(sessionId, p, qty);
            ConsoleColors.success(p.getName() + " × " + qty + " added to cart 🛒");
        } catch (Exception e) { ConsoleColors.error(e.getMessage()); }
    }

    // ── Cart ──────────────────────────────────────────────────────────

    private void viewCart() {
        List<CartItem> cart = orderService.getCart(sessionId);
        if (cart.isEmpty()) { ConsoleColors.info("Your cart is empty."); return; }

        ConsoleColors.header("MY CART");
        System.out.printf("%n  %-35s %9s %5s %12s%n", "Product", "Price(₹)", "Qty", "Subtotal(₹)");
        ConsoleColors.divider();
        cart.forEach(ci ->
                System.out.printf("  %-35s %9.2f %5d %12.2f%n",
                        ci.getProduct().getName(), ci.getProduct().getPrice(),
                        ci.getQuantity(), ci.getSubtotal()));
        ConsoleColors.divider();
        System.out.printf("  %-51s %12.2f%n", "TOTAL", orderService.getCartTotal(sessionId));

        System.out.println("\n  1. Remove an item   0. Back");
        System.out.print("  → ");
        if (sc.nextLine().trim().equals("1")) {
            int pid = InputValidator.readInt(sc, "  Product ID to remove: ", 1, Integer.MAX_VALUE);
            orderService.removeFromCart(sessionId, pid);
            ConsoleColors.success("Item removed.");
        }
    }

    // ── Checkout ──────────────────────────────────────────────────────

    private void checkout() {
        if (orderService.getCart(sessionId).isEmpty()) {
            ConsoleColors.info("Your cart is empty — add items first.");
            return;
        }
        if (customerProfile == null) {
            ConsoleColors.error("No customer profile found. Please contact support.");
            return;
        }

        ConsoleColors.header("CHECKOUT");
        BigDecimal total = orderService.getCartTotal(sessionId);
        System.out.printf("  Order Total     : ₹%.2f%n", total);
        System.out.printf("  Delivery Address: %s, %s%n",
                customerProfile.getAddress(), customerProfile.getCity());

        System.out.println("\n  Payment Method:");
        System.out.println("    1. Cash on Delivery");
        System.out.println("    2. UPI");
        System.out.println("    3. Credit / Debit Card");
        String paymentMethod = switch (InputValidator.readInt(sc, "  → ", 1, 3)) {
            case 1 -> "Cash on Delivery";
            case 2 -> "UPI";
            default -> "Credit/Debit Card";
        };

        if (InputValidator.readYesNo(sc, "\n  Confirm and place order?")) {
            try {
                String addr = customerProfile.getAddress() + ", " + customerProfile.getCity();
                Order order = orderService.placeOrder(sessionId, customerProfile.getId(),
                        addr, paymentMethod);
                ConsoleColors.success("Order #" + order.getOrderId() +
                        " placed! Total: ₹" + order.getTotalAmount() +
                        "  Payment: " + paymentMethod);
            } catch (Exception e) {
                ConsoleColors.error("Order failed: " + e.getMessage());
            }
        } else {
            ConsoleColors.info("Order cancelled — cart preserved.");
        }
    }

    // ── My Orders ─────────────────────────────────────────────────────

    private void myOrders() {
        if (customerProfile == null) { ConsoleColors.error("Profile not linked."); return; }
        List<Order> orders = orderService.getCustomerOrders(customerProfile.getId());
        if (orders.isEmpty()) { ConsoleColors.info("You have no orders yet."); return; }

        ConsoleColors.header("MY ORDERS");
        orders.forEach(o -> System.out.println("  " + o));

        System.out.print("\n  Enter Order ID for details (0 = back): ");
        String in = sc.nextLine().trim();
        if (!in.equals("0")) {
            try {
                orderService.getOrder(Integer.parseInt(in)).ifPresentOrElse(o -> {
                    System.out.println("\n  " + o);
                    o.getItems().forEach(i -> System.out.println(i));
                }, () -> ConsoleColors.error("Order not found."));
            } catch (NumberFormatException e) { ConsoleColors.warning("Invalid ID."); }
        }
    }

    // ── My Profile ────────────────────────────────────────────────────

    private void myProfile() {
        if (customerProfile == null) {
            ConsoleColors.info("No profile linked to your account.");
            return;
        }
        ConsoleColors.header("MY PROFILE");
        System.out.println("\n  " + customerProfile.getDetails());
        System.out.println("\n  1. Edit Profile   0. Back");
        System.out.print("  → ");
        if (!sc.nextLine().trim().equals("1")) return;

        System.out.printf("  Phone   [%s]: ", customerProfile.getPhone());
        String v = sc.nextLine().trim(); if (!v.isBlank()) customerProfile.setPhone(v);

        System.out.printf("  Address [%s]: ", customerProfile.getAddress());
        v = sc.nextLine().trim(); if (!v.isBlank()) customerProfile.setAddress(v);

        System.out.printf("  City    [%s]: ", customerProfile.getCity());
        v = sc.nextLine().trim(); if (!v.isBlank()) customerProfile.setCity(v);

        try {
            customerService.updateCustomer(customerProfile);
            ConsoleColors.success("Profile updated.");
        } catch (Exception e) { ConsoleColors.error(e.getMessage()); }
    }
}
