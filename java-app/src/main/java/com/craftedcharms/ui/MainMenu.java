package com.craftedcharms.ui;

import java.util.Scanner;

import com.craftedcharms.db.DatabaseConnection;
import com.craftedcharms.model.Role;
import com.craftedcharms.model.User;
import com.craftedcharms.service.AuthService;
import com.craftedcharms.util.ConsoleColors;
import com.craftedcharms.util.InputValidator;

/**
 * Presentation Layer — Application entry point / main menu.
 * Displays login and registration options; routes to AdminMenu or CustomerMenu
 * based on the authenticated user's Role.
 *
 * Demonstrates: Layered architecture (Presentation Layer).
 */
public class MainMenu {

    private final AuthService authService;
    private final Scanner     sc;

    public MainMenu() {
        this.authService = new AuthService();
        this.sc          = new Scanner(System.in);
    }

    /** Start the application — runs until the user exits. */
    public void start() {
        printBanner();
        mainLoop();
        DatabaseConnection.getInstance().close();
        sc.close();
    }

    private void mainLoop() {
        while (true) {
            ConsoleColors.header("WELCOME  ·  CRAFTED CHARMS");
            System.out.println("    1. Login");
            System.out.println("    2. Register as New Customer");
            System.out.println("    0. Exit");
            System.out.print("\n  → ");
            switch (sc.nextLine().trim()) {
                case "1" -> handleLogin();
                case "2" -> handleRegister();
                case "0" -> {
                    ConsoleColors.info("Thank you for visiting Crafted Charms. Goodbye! 🪷");
                    return;
                }
                default -> ConsoleColors.warning("Invalid option — enter 1, 2, or 0.");
            }
        }
    }

    private void handleLogin() {
        ConsoleColors.header("LOGIN");
        try {
            String username = InputValidator.readNonEmpty(sc, "  Username : ");
            System.out.print("  Password  : ");
            String password = sc.nextLine().trim();

            User user = authService.login(username, password);
            if (user == null) {
                ConsoleColors.error("Invalid username or password.");
                return;
            }
            ConsoleColors.success("Welcome back, " + user.getFullName() + "!  (" + user.getRole() + ")");

            // RBAC routing — demonstrates Role-Based Access Control
            if (user.getRole() == Role.ADMIN) {
                new AdminMenu(authService, sc).show();
            } else {
                new CustomerMenu(authService, sc).show();
            }
        } catch (Exception e) {
            ConsoleColors.error("Login error: " + e.getMessage());
        }
    }

    private void handleRegister() {
        ConsoleColors.header("NEW CUSTOMER REGISTRATION");
        try {
            String fullName = InputValidator.readNonEmpty(sc, "  Full Name    : ");
            String email    = InputValidator.readEmail(sc,    "  Email        : ");
            String username = InputValidator.readNonEmpty(sc, "  Username     : ");
            System.out.print("  Password      : ");
            String password = sc.nextLine().trim();
            if (password.length() < 8) {
                ConsoleColors.error("Password must be at least 8 characters.");
                return;
            }
            String phone   = InputValidator.readPhone(sc,     "  Phone        : ");
            String address = InputValidator.readNonEmpty(sc,  "  Address      : ");
            String city    = InputValidator.readNonEmpty(sc,  "  City         : ");

            authService.registerCustomer(fullName, email, username, password, phone, address, city);
            ConsoleColors.success("Registration successful!  You can now login with username: " + username);
        } catch (Exception e) {
            ConsoleColors.error("Registration failed: " + e.getMessage());
        }
    }

    // ── ASCII / Unicode banner ────────────────────────────────────────

    private void printBanner() {
        System.out.println(ConsoleColors.BRIGHT_PURPLE);
        System.out.println("  ╔══════════════════════════════════════════════════════╗");
        System.out.println("  ║                                                      ║");
        System.out.println("  ║       🪷   C R A F T E D   C H A R M S   🪷         ║");
        System.out.println("  ║          Handmade Jewellery — Mumbai, India          ║");
        System.out.println("  ║                                                      ║");
        System.out.println("  ║         Java + JDBC + MySQL  |  v1.0.0              ║");
        System.out.println("  ║                                                      ║");
        System.out.println("  ╚══════════════════════════════════════════════════════╝");
        System.out.println(ConsoleColors.RESET);
    }
}
