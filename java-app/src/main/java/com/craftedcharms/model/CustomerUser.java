package com.craftedcharms.model;

/**
 * Specialised User for customer-role accounts.
 * Extends User — demonstrates Inheritance and Method overriding.
 * Tracks loyalty points as a customer-specific feature.
 */
public class CustomerUser extends User {

    private int loyaltyPoints;

    // Default constructor
    public CustomerUser() { super(); }

    // Parameterised constructor
    public CustomerUser(int id, String fullName, String email,
                        String username, String passwordHash) {
        super(id, fullName, email, username, passwordHash, Role.CUSTOMER);
        this.loyaltyPoints = 0;
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public int  getLoyaltyPoints()              { return loyaltyPoints; }
    public void setLoyaltyPoints(int points)    { this.loyaltyPoints = points; }

    // Customer-specific behaviour
    public void addLoyaltyPoints(int points) {
        this.loyaltyPoints += points;
        System.out.println("🌟 +" + points + " loyalty points added! Total: " + this.loyaltyPoints);
    }

    // ── Method overriding ──────────────────────────────────────────────

    @Override
    public String getDetails() {
        return super.getDetails() + " | Loyalty Points: " + loyaltyPoints;
    }
}
