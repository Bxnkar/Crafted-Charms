package com.craftedcharms.model;

import java.time.LocalDateTime;

/**
 * Represents a login account in the system.
 * Extends Person — demonstrates Inheritance.
 * Holds role for RBAC, hashed password for security.
 */
public class User extends Person {

    private String username;
    private String passwordHash;   // Stored password hash (PBKDF2 format; legacy hashes supported)
    private Role role;
    private LocalDateTime createdAt;

    // Default constructor
    public User() {}

    // Parameterised constructor — demonstrates Constructor usage
    public User(int id, String fullName, String email,
                String username, String passwordHash, Role role) {
        super(id, fullName, email);
        this.username     = username;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.createdAt    = LocalDateTime.now();
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public String getUsername()                   { return username; }
    public void   setUsername(String username)    { this.username = username; }

    public String getPasswordHash()                      { return passwordHash; }
    public void   setPasswordHash(String passwordHash)   { this.passwordHash = passwordHash; }

    public Role getRole()          { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt()                    { return createdAt; }
    public void          setCreatedAt(LocalDateTime t)     { this.createdAt = t; }

    // ── Abstract method implementation — Method overriding ─────────────

    @Override
    public String getDetails() {
        return String.format("User [%d] | Username: %-15s | Role: %-8s | Email: %s",
                getId(), username, role, getEmail());
    }
}
