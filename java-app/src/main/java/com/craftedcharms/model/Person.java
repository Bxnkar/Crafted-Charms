package com.craftedcharms.model;

/**
 * Abstract base class for all persons in the system (Users and Customers).
 * Demonstrates: Abstract Class, Encapsulation, Constructor usage.
 */
public abstract class Person {

    private int id;
    private String fullName;
    private String email;

    // Default constructor
    public Person() {}

    // Parameterised constructor
    public Person(int id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // ── Abstract method — forces subclasses to describe themselves ─────

    /**
     * Returns a formatted string with this person's details.
     * Demonstrates: Abstract method / Method overriding.
     */
    public abstract String getDetails();

    @Override
    public String toString() {
        return getDetails();
    }
}
