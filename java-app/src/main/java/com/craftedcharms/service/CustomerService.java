package com.craftedcharms.service;

import com.craftedcharms.dao.CustomerDAO;
import com.craftedcharms.dao.CustomerDAOImpl;
import com.craftedcharms.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Business Logic Layer — Customer profile management.
 * Validates customer data before delegating to CustomerDAO.
 */
public class CustomerService {

    private final CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAOImpl();
    }

    // ── CREATE ────────────────────────────────────────────────────────

    public void addCustomer(Customer customer) {
        validate(customer);
        customerDAO.addCustomer(customer);
    }

    // ── READ ──────────────────────────────────────────────────────────

    public Optional<Customer> getCustomer(int customerId) {
        return customerDAO.getCustomerById(customerId);
    }

    public Optional<Customer> getCustomerByUserId(int userId) {
        return customerDAO.getCustomerByUserId(userId);
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.getAllCustomers();
    }

    public List<Customer> searchByName(String name) {
        return customerDAO.searchByName(name);
    }

    public List<Customer> searchByCity(String city) {
        return customerDAO.searchByCity(city);
    }

    // ── UPDATE ────────────────────────────────────────────────────────

    public void updateCustomer(Customer customer) {
        validate(customer);
        ensureExists(customer.getId());
        customerDAO.updateCustomer(customer);
    }

    // ── DELETE ────────────────────────────────────────────────────────

    public void removeCustomer(int customerId) {
        ensureExists(customerId);
        customerDAO.deleteCustomer(customerId);
    }

    // ── Validation ────────────────────────────────────────────────────

    private void validate(Customer c) {
        if (c.getFullName() == null || c.getFullName().isBlank())
            throw new IllegalArgumentException("Customer name cannot be empty.");
        if (c.getEmail() == null || !c.getEmail().contains("@"))
            throw new IllegalArgumentException("Invalid email address.");
        if (c.getPhone() == null || c.getPhone().replaceAll("\\D", "").length() < 10)
            throw new IllegalArgumentException("Phone number must be at least 10 digits.");
        if (c.getCity() == null || c.getCity().isBlank())
            throw new IllegalArgumentException("City cannot be empty.");
    }

    private void ensureExists(int customerId) {
        if (customerDAO.getCustomerById(customerId).isEmpty())
            throw new IllegalArgumentException("Customer with ID " + customerId + " not found.");
    }
}
