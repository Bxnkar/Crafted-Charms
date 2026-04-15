package com.craftedcharms.dao;

import com.craftedcharms.model.User;

import java.util.List;
import java.util.Optional;

/**
 * DAO Interface for User account CRUD operations.
 * Demonstrates: Interface usage (OOP requirement).
 * Implemented by UserDAOImpl.
 */
public interface UserDAO {

    /** Insert a new user; sets generated userId back on the object. */
    void addUser(User user);

    /** Fetch a user by primary key. */
    Optional<User> getUserById(int userId);

    /** Fetch a user by username (for login lookup). */
    Optional<User> getUserByUsername(String username);

    /** Fetch all users. */
    List<User> getAllUsers();

    /** Update mutable fields (fullName, email, role). */
    void updateUser(User user);

    /** Hard-delete a user record. */
    void deleteUser(int userId);

    /** Returns true if username is already taken. */
    boolean usernameExists(String username);

    /** Returns true if email is already registered. */
    boolean emailExists(String email);
}
