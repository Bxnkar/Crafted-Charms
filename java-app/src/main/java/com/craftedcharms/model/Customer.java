package com.craftedcharms.model;

import java.time.LocalDateTime;

/**
 * Represents a customer's profile (shipping info, contact).
 * Extends Person — demonstrates Inheritance.
 * Linked to a User account via userId.
 */
public class Customer extends Person {

    private int userId;         // FK → users.user_id
    private String phone;
    private String address;
    private String city;
    private LocalDateTime createdAt;

    // Default constructor
    public Customer() {}

    // Parameterised constructor
    public Customer(int customerId, int userId, String fullName,
                    String email, String phone, String address, String city) {
        super(customerId, fullName, email);
        this.userId    = userId;
        this.phone     = phone;
        this.address   = address;
        this.city      = city;
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public int  getUserId()             { return userId; }
    public void setUserId(int userId)   { this.userId = userId; }

    public String getPhone()                { return phone; }
    public void   setPhone(String phone)   { this.phone = phone; }

    public String getAddress()                  { return address; }
    public void   setAddress(String address)   { this.address = address; }

    public String getCity()               { return city; }
    public void   setCity(String city)    { this.city = city; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void          setCreatedAt(LocalDateTime t)  { this.createdAt = t; }

    // ── Method overriding ──────────────────────────────────────────────

    @Override
    public String getDetails() {
        return String.format(
                "Customer [%d] | Name: %-20s | Email: %-25s | Phone: %-12s | City: %s",
                getId(), getFullName(), getEmail(), phone, city);
    }
}
