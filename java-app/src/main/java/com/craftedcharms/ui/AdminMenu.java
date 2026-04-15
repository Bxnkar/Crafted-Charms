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
 * Presentation Layer — Admin Dashboard.
 * Provides full CRUD menus for Products, Customers, Orders, and Users.
 * Only accessible to users with Role.ADMIN.
 */
public class AdminMenu {

    private final AuthService     authService;
    private final ProductService  productService;
    private final CustomerService customerService;
    private final OrderService    orderService;
    private final Scanner sc;

    public AdminMenu(AuthService authService, Scanner sc) {
        this.authService     = authService;
        this.productService  = new ProductService();
        this.customerService = new CustomerService();
        this.orderService    = new OrderService();
        this.sc              = sc;
    }

    public void show() {
        while (true) {
            ConsoleColors.header("ADMIN DASHBOARD  ·  " + authService.getCurrentUser().getFullName());
            System.out.println("    1. Product Management");
            System.out.println("    2. Customer Management");
            System.out.println("    3. Order Management");
            System.out.println("    4. Create Admin Account");
            System.out.println("    0. Logout");
            System.out.print("\n  → ");
            switch (sc.nextLine().trim()) {
                case "1" -> productMenu();
                case "2" -> customerMenu();
                case "3" -> orderMenu();
                case "4" -> createAdmin();
                case "0" -> { authService.logout(); ConsoleColors.info("Logged out."); return; }
                default  -> ConsoleColors.warning("Invalid option — try again.");
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PRODUCT MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    private void productMenu() {
        while (true) {
            ConsoleColors.header("PRODUCT MANAGEMENT");
            System.out.println("    1. Add New Product");
            System.out.println("    2. View All Products");
            System.out.println("    3. Search Products");
            System.out.println("    4. Update Product");
            System.out.println("    5. Deactivate Product");
            System.out.println("    6. Update Stock");
            System.out.println("    0. Back");
            System.out.print("\n  → ");
            switch (sc.nextLine().trim()) {
                case "1" -> addProduct();
                case "2" -> viewAllProducts();
                case "3" -> searchProducts();
                case "4" -> updateProduct();
                case "5" -> deactivateProduct();
                case "6" -> updateStock();
                case "0" -> { return; }
                default  -> ConsoleColors.warning("Invalid option.");
            }
        }
    }

    private void addProduct() {
        ConsoleColors.header("ADD NEW PRODUCT");
        try {
            String name = InputValidator.readNonEmpty(sc, "  Name              : ");
            String desc = InputValidator.readNonEmpty(sc, "  Description       : ");
            System.out.println("  Category  →  1=RING  2=NECKLACE  3=BRACELET  4=EARRING");
            Category cat = switch (InputValidator.readInt(sc, "  → ", 1, 4)) {
                case 1 -> Category.RING;
                case 2 -> Category.NECKLACE;
                case 3 -> Category.BRACELET;
                default -> Category.EARRING;
            };
            String material = InputValidator.readNonEmpty(sc, "  Material          : ");
            String gemstone = InputValidator.readOptional(sc, "  Gemstone (opt)    : ");
            BigDecimal price = InputValidator.readPrice(sc, "  Price (₹)         : ");
            int stock = InputValidator.readInt(sc, "  Stock Quantity     : ", 0, 99999);

            JewelryProduct p = new JewelryProduct(0, name, desc, price, stock,
                    material, gemstone.isBlank() ? null : gemstone, cat);
            productService.addProduct(p);
            ConsoleColors.success("Product '" + name + "' added with ID " + p.getProductId());
        } catch (Exception e) {
            ConsoleColors.error(e.getMessage());
        }
    }

    private void viewAllProducts() {
        List<JewelryProduct> list = productService.getAllProducts();
        if (list.isEmpty()) { ConsoleColors.info("No products found."); return; }
        ConsoleColors.header("ALL PRODUCTS  (" + list.size() + ")");
        printProductTable(list);
    }

    private void searchProducts() {
        ConsoleColors.header("SEARCH PRODUCTS");
        System.out.println("    1. By Name");
        System.out.println("    2. By Category");
        System.out.println("    3. By Name + Category");
        System.out.println("    4. By Name + Category + Max Price");
        System.out.print("  → ");
        try {
            List<JewelryProduct> results = switch (sc.nextLine().trim()) {
                case "1" -> productService.search(InputValidator.readNonEmpty(sc, "  Keyword: "));
                case "2" -> productService.search("", InputValidator.readNonEmpty(sc,
                            "  Category (RING/NECKLACE/BRACELET/EARRING): "));
                case "3" -> productService.search(
                            InputValidator.readNonEmpty(sc, "  Keyword : "),
                            InputValidator.readNonEmpty(sc, "  Category: "));
                case "4" -> productService.search(
                            InputValidator.readOptional(sc,  "  Keyword  (opt): "),
                            InputValidator.readOptional(sc,  "  Category (opt): "),
                            InputValidator.readPrice(sc,     "  Max Price (₹)  : "));
                default -> { ConsoleColors.warning("Invalid."); yield List.of(); }
            };
            if (results.isEmpty()) ConsoleColors.info("No matching products.");
            else printProductTable(results);
        } catch (Exception e) {
            ConsoleColors.error(e.getMessage());
        }
    }

    private void updateProduct() {
        ConsoleColors.header("UPDATE PRODUCT");
        viewAllProducts();
        try {
            int id = InputValidator.readInt(sc, "\n  Product ID to edit: ", 1, Integer.MAX_VALUE);
            Optional<JewelryProduct> opt = productService.getProduct(id);
            if (opt.isEmpty()) { ConsoleColors.error("Product not found."); return; }
            JewelryProduct p = opt.get();
            System.out.println("  (Press Enter to keep current value)");

            System.out.printf("  Name       [%s]: ", p.getName());
            String v = sc.nextLine().trim(); if (!v.isBlank()) p.setName(v);

            System.out.printf("  Description[%s]: ", abbr(p.getDescription(), 30));
            v = sc.nextLine().trim(); if (!v.isBlank()) p.setDescription(v);

            System.out.printf("  Material   [%s]: ", p.getMaterial());
            v = sc.nextLine().trim(); if (!v.isBlank()) p.setMaterial(v);

            System.out.printf("  Price      [%.2f]: ", p.getPrice());
            v = sc.nextLine().trim();
            if (!v.isBlank()) { try { p.setPrice(new BigDecimal(v)); } catch (Exception ignored) {} }

            System.out.printf("  Stock      [%d]: ", p.getStockQty());
            v = sc.nextLine().trim();
            if (!v.isBlank()) { try { p.setStockQty(Integer.parseInt(v)); } catch (Exception ignored) {} }

            productService.updateProduct(p);
            ConsoleColors.success("Product #" + id + " updated.");
        } catch (Exception e) {
            ConsoleColors.error(e.getMessage());
        }
    }

    private void deactivateProduct() {
        try {
            int id = InputValidator.readInt(sc, "  Product ID to deactivate: ", 1, Integer.MAX_VALUE);
            if (InputValidator.readYesNo(sc, "  Confirm deactivate product #" + id + "?")) {
                productService.removeProduct(id);
                ConsoleColors.success("Product #" + id + " deactivated.");
            }
        } catch (Exception e) { ConsoleColors.error(e.getMessage()); }
    }

    private void updateStock() {
        try {
            int id  = InputValidator.readInt(sc, "  Product ID  : ", 1, Integer.MAX_VALUE);
            int qty = InputValidator.readInt(sc, "  New Stock   : ", 0, 99999);
            productService.updateStock(id, qty);
            ConsoleColors.success("Stock updated to " + qty + ".");
        } catch (Exception e) { ConsoleColors.error(e.getMessage()); }
    }

    private void printProductTable(List<JewelryProduct> list) {
        System.out.printf("%n  %-4s %-30s %-10s %-18s %9s %5s %6s%n",
                "ID", "Name", "Category", "Material", "Price(₹)", "Qty", "Active");
        ConsoleColors.divider();
        for (JewelryProduct p : list) {
            System.out.printf("  %-4d %-30s %-10s %-18s %9.2f %5d %6s%n",
                    p.getProductId(), abbr(p.getName(), 29), p.getCategory(),
                    abbr(p.getMaterial(), 17), p.getPrice(), p.getStockQty(),
                    p.isActive() ? "✓" : "—");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CUSTOMER MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    private void customerMenu() {
        while (true) {
            ConsoleColors.header("CUSTOMER MANAGEMENT");
            System.out.println("    1. View All Customers");
            System.out.println("    2. Search Customers");
            System.out.println("    3. View Customer Details");
            System.out.println("    4. Update Customer");
            System.out.println("    5. Delete Customer");
            System.out.println("    0. Back");
            System.out.print("\n  → ");
            switch (sc.nextLine().trim()) {
                case "1" -> viewAllCustomers();
                case "2" -> searchCustomers();
                case "3" -> viewCustomerDetails();
                case "4" -> updateCustomer();
                case "5" -> deleteCustomer();
                case "0" -> { return; }
                default  -> ConsoleColors.warning("Invalid option.");
            }
        }
    }

    private void viewAllCustomers() {
        List<Customer> list = customerService.getAllCustomers();
        if (list.isEmpty()) { ConsoleColors.info("No customers found."); return; }
        ConsoleColors.header("ALL CUSTOMERS  (" + list.size() + ")");
        System.out.printf("%n  %-4s %-22s %-25s %-13s %-12s%n",
                "ID", "Name", "Email", "Phone", "City");
        ConsoleColors.divider();
        for (Customer c : list) {
            System.out.printf("  %-4d %-22s %-25s %-13s %-12s%n",
                    c.getId(), abbr(c.getFullName(), 21), abbr(c.getEmail(), 24),
                    c.getPhone(), c.getCity());
        }
    }

    private void searchCustomers() {
        System.out.println("  1. By Name   2. By City");
        System.out.print("  → ");
        try {
            List<Customer> results = sc.nextLine().trim().equals("1")
                    ? customerService.searchByName(InputValidator.readNonEmpty(sc, "  Name keyword: "))
                    : customerService.searchByCity(InputValidator.readNonEmpty(sc, "  City: "));
            if (results.isEmpty()) ConsoleColors.info("No customers found.");
            else results.forEach(c -> System.out.println("  " + c.getDetails()));
        } catch (Exception e) { ConsoleColors.error(e.getMessage()); }
    }

    private void viewCustomerDetails() {
        int id = InputValidator.readInt(sc, "  Customer ID: ", 1, Integer.MAX_VALUE);
        customerService.getCustomer(id).ifPresentOrElse(
                c -> System.out.println("\n  " + c.getDetails()),
                () -> ConsoleColors.error("Customer not found."));
    }

    private void updateCustomer() {
        int id = InputValidator.readInt(sc, "  Customer ID to update: ", 1, Integer.MAX_VALUE);
        Optional<Customer> opt = customerService.getCustomer(id);
        if (opt.isEmpty()) { ConsoleColors.error("Customer not found."); return; }
        Customer c = opt.get();
        System.out.println("  (Press Enter to keep current value)");
        System.out.printf("  Name    [%s]: ", c.getFullName());
        String v = sc.nextLine().trim(); if (!v.isBlank()) c.setFullName(v);
        System.out.printf("  Phone   [%s]: ", c.getPhone());
        v = sc.nextLine().trim(); if (!v.isBlank()) c.setPhone(v);
        System.out.printf("  Address [%s]: ", abbr(c.getAddress(), 30));
        v = sc.nextLine().trim(); if (!v.isBlank()) c.setAddress(v);
        System.out.printf("  City    [%s]: ", c.getCity());
        v = sc.nextLine().trim(); if (!v.isBlank()) c.setCity(v);
        try { customerService.updateCustomer(c); ConsoleColors.success("Customer updated."); }
        catch (Exception e) { ConsoleColors.error(e.getMessage()); }
    }

    private void deleteCustomer() {
        try {
            int id = InputValidator.readInt(sc, "  Customer ID to delete: ", 1, Integer.MAX_VALUE);
            if (InputValidator.readYesNo(sc, "  Confirm delete customer #" + id + "?")) {
                customerService.removeCustomer(id);
                ConsoleColors.success("Customer #" + id + " deleted.");
            }
        } catch (Exception e) { ConsoleColors.error(e.getMessage()); }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ORDER MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    private void orderMenu() {
        while (true) {
            ConsoleColors.header("ORDER MANAGEMENT");
            System.out.println("    1. View All Orders");
            System.out.println("    2. View Order Details");
            System.out.println("    3. Update Order Status");
            System.out.println("    4. Cancel Order");
            System.out.println("    5. Orders by Customer");
            System.out.println("    0. Back");
            System.out.print("\n  → ");
            switch (sc.nextLine().trim()) {
                case "1" -> viewAllOrders();
                case "2" -> viewOrderDetails();
                case "3" -> updateOrderStatus();
                case "4" -> cancelOrder();
                case "5" -> ordersByCustomer();
                case "0" -> { return; }
                default  -> ConsoleColors.warning("Invalid option.");
            }
        }
    }

    private void viewAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        if (orders.isEmpty()) { ConsoleColors.info("No orders found."); return; }
        ConsoleColors.header("ALL ORDERS  (" + orders.size() + ")");
        System.out.printf("%n  %-7s %-8s %-12s %11s %-11s%n",
                "OrderID", "CustID", "Date", "Amount(₹)", "Status");
        ConsoleColors.divider();
        for (Order o : orders) {
            System.out.printf("  %-7d %-8d %-12s %11.2f %-11s%n",
                    o.getOrderId(), o.getCustomerId(),
                    o.getOrderDate() != null ? o.getOrderDate().toLocalDate() : "—",
                    o.getTotalAmount(), o.getStatus());
        }
    }

    private void viewOrderDetails() {
        int id = InputValidator.readInt(sc, "  Order ID: ", 1, Integer.MAX_VALUE);
        orderService.getOrder(id).ifPresentOrElse(o -> {
            System.out.println("\n  " + o);
            System.out.println("  Items:");
            o.getItems().forEach(i -> System.out.println(i));
        }, () -> ConsoleColors.error("Order not found."));
    }

    private void updateOrderStatus() {
        try {
            int id = InputValidator.readInt(sc, "  Order ID: ", 1, Integer.MAX_VALUE);
            System.out.println("  1=CONFIRMED  2=SHIPPED  3=DELIVERED  4=CANCELLED");
            OrderStatus status = switch (InputValidator.readInt(sc, "  → ", 1, 4)) {
                case 1 -> OrderStatus.CONFIRMED;
                case 2 -> OrderStatus.SHIPPED;
                case 3 -> OrderStatus.DELIVERED;
                default -> OrderStatus.CANCELLED;
            };
            orderService.updateStatus(id, status);
            ConsoleColors.success("Order #" + id + " → " + status);
        } catch (Exception e) { ConsoleColors.error(e.getMessage()); }
    }

    private void cancelOrder() {
        try {
            int id = InputValidator.readInt(sc, "  Order ID to cancel: ", 1, Integer.MAX_VALUE);
            if (InputValidator.readYesNo(sc, "  Confirm cancel order #" + id + "?")) {
                orderService.cancelOrder(id);
                ConsoleColors.success("Order #" + id + " cancelled.");
            }
        } catch (Exception e) { ConsoleColors.error(e.getMessage()); }
    }

    private void ordersByCustomer() {
        int custId = InputValidator.readInt(sc, "  Customer ID: ", 1, Integer.MAX_VALUE);
        List<Order> orders = orderService.getCustomerOrders(custId);
        if (orders.isEmpty()) ConsoleColors.info("No orders for customer #" + custId);
        else orders.forEach(o -> System.out.println("  " + o));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  USER MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    private void createAdmin() {
        ConsoleColors.header("CREATE ADMIN ACCOUNT");
        try {
            String fullName = InputValidator.readNonEmpty(sc, "  Full Name : ");
            String email    = InputValidator.readEmail(sc,    "  Email     : ");
            String username = InputValidator.readNonEmpty(sc, "  Username  : ");
            System.out.print("  Password   : ");
            String password = sc.nextLine().trim();
            authService.registerAdmin(fullName, email, username, password);
            ConsoleColors.success("Admin account '" + username + "' created.");
        } catch (Exception e) { ConsoleColors.error(e.getMessage()); }
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private String abbr(String s, int len) {
        if (s == null || s.isBlank()) return "—";
        return s.length() <= len ? s : s.substring(0, len - 2) + "..";
    }
}
