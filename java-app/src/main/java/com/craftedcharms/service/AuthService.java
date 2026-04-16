package com.craftedcharms.service;

import java.util.Optional;

import com.craftedcharms.dao.CustomerDAO;
import com.craftedcharms.dao.CustomerDAOImpl;
import com.craftedcharms.dao.UserDAO;
import com.craftedcharms.dao.UserDAOImpl;
import com.craftedcharms.model.AdminUser;
import com.craftedcharms.model.Customer;
import com.craftedcharms.model.CustomerUser;
import com.craftedcharms.model.Role;
import com.craftedcharms.model.User;
import com.craftedcharms.util.PasswordHasher;

/**
 * Business Logic Layer — Authentication and authorisation.
 * Manages login, registration, logout, password hashing, and RBAC.
 * Demonstrates: Service layer pattern, password hashing, Role-based access.
 */
public class AuthService {

    private final UserDAO     userDAO;
    private final CustomerDAO customerDAO;
    private User currentUser;   // session state (in-memory)

    public AuthService() {
        this.userDAO     = new UserDAOImpl();
        this.customerDAO = new CustomerDAOImpl();
    }

    // ── Authentication ────────────────────────────────────────────────

    /**
     * Verifies credentials and returns the User if valid; otherwise null.
     * Password is hashed before comparison — never stored in plain text.
     */
    public User login(String username, String password) {
        Optional<User> opt = userDAO.getUserByUsername(username.trim());
        if (opt.isPresent()) {
            User user = opt.get();
            if (PasswordHasher.verify(password, user.getPasswordHash())) {
                // Migrate old SHA-256 hashes to PBKDF2 after successful login.
                if (PasswordHasher.isLegacySha256(user.getPasswordHash())) {
                    user.setPasswordHash(hashPassword(password));
                    userDAO.updateUser(user);
                }
                this.currentUser = user;
                return user;
            }
        }
        return null;
    }

    public void logout() {
        this.currentUser = null;
    }

    // ── Registration ──────────────────────────────────────────────────

    /** Register a new ADMIN account. */
    public User registerAdmin(String fullName, String email, String username, String password) {
        validateNewAccount(username, email);
        validateNewPassword(password);
        AdminUser admin = new AdminUser(0, fullName, email, username, hashPassword(password));
        userDAO.addUser(admin);
        return admin;
    }

    /**
     * Register a new CUSTOMER — creates both a User account and a Customer profile.
     * Demonstrates: method coordinating multiple DAO operations.
     */
    public User registerCustomer(String fullName, String email, String username,
                                 String password, String phone, String address, String city) {
        validateNewAccount(username, email);
        validateNewPassword(password);
        CustomerUser cu = new CustomerUser(0, fullName, email, username, hashPassword(password));
        userDAO.addUser(cu);
        // Create linked customer profile
        Customer profile = new Customer(0, cu.getId(), fullName, email, phone, address, city);
        customerDAO.addCustomer(profile);
        return cu;
    }

    // ── Password management ───────────────────────────────────────────

    public void changePassword(int userId, String oldPassword, String newPassword) {
        User user = userDAO.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!PasswordHasher.verify(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        validateNewPassword(newPassword);
        user.setPasswordHash(hashPassword(newPassword));
        userDAO.updateUser(user);
    }

    /**
     * Hashes a plain-text password for storage using PBKDF2.
     * Static so it can be called during sample-data seeding.
     */
    public static String hashPassword(String password) {
        return PasswordHasher.hashForStorage(password);
    }

    // ── Session helpers ───────────────────────────────────────────────

    public User    getCurrentUser() { return currentUser; }
    public boolean isLoggedIn()     { return currentUser != null; }
    public boolean isAdmin()        { return currentUser != null && currentUser.getRole() == Role.ADMIN; }
<<<<<<< Updated upstream
=======
    public boolean hasAnyUsers()    { return !userDAO.getAllUsers().isEmpty(); }
>>>>>>> Stashed changes

    // ── Validation ────────────────────────────────────────────────────

    private void validateNewAccount(String username, String email) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username cannot be empty.");
        if (userDAO.usernameExists(username))
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        if (userDAO.emailExists(email))
            throw new IllegalArgumentException("Email '" + email + "' is already registered.");
    }

    private void validateNewPassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
    }
}
