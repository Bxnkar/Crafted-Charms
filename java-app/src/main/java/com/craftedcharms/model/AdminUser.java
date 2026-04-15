package com.craftedcharms.model;

/**
 * Specialised User for admin-role accounts.
 * Extends User — demonstrates Inheritance and Method overriding.
 */
public class AdminUser extends User {

    private String department;

    // Default constructor
    public AdminUser() { super(); }

    // Constructor without department
    public AdminUser(int id, String fullName, String email,
                     String username, String passwordHash) {
        super(id, fullName, email, username, passwordHash, Role.ADMIN);
        this.department = "Management";
    }

    // Constructor with department — demonstrates Method overloading
    public AdminUser(int id, String fullName, String email,
                     String username, String passwordHash, String department) {
        super(id, fullName, email, username, passwordHash, Role.ADMIN);
        this.department = department;
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public String getDepartment()                  { return department; }
    public void   setDepartment(String department) { this.department = department; }

    // Admin-specific behaviour
    public void generateReport() {
        System.out.println("📊 Generating admin report for: " + getFullName());
    }

    // ── Method overriding ──────────────────────────────────────────────

    @Override
    public String getDetails() {
        return super.getDetails() + " | Dept: " + department;
    }
}
