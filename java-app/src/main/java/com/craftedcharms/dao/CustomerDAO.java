package com.craftedcharms.dao;

import com.craftedcharms.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * DAO Interface for Customer CRUD operations.
 * Demonstrates: Interface usage (OOP requirement).
 * Implemented by CustomerDAOImpl.
 */
public interface CustomerDAO {

    /** Insert a new customer; sets generated customerId back on the object. */
    void addCustomer(Customer customer);

    /** Fetch a customer by primary key. */
    Optional<Customer> getCustomerById(int customerId);

    /** Fetch a customer by the linked user_id (for login → profile lookup). */
    Optional<Customer> getCustomerByUserId(int userId);

    /** Fetch all customers. */
    List<Customer> getAllCustomers();

    /** Update mutable fields of an existing customer. */
    void updateCustomer(Customer customer);

    /** Hard-delete a customer record. */
    void deleteCustomer(int customerId);

    /** LIKE search by full name. */
    List<Customer> searchByName(String name);

    /** LIKE search by city. */
    List<Customer> searchByCity(String city);
}
