package com.craftedcharms.exception;

/**
 * Custom unchecked exception for all database-related failures.
 *
 * Replaces bare RuntimeException wrapping in every DAO class.
 * Demonstrates: Custom Exception class (good OOP practice).
 *
 * Fix for: ⚠3 — Weak exception handling (bare RuntimeException).
 */
public class DatabaseException extends RuntimeException {

    /** The name of the DAO operation that failed (e.g. "addProduct"). */
    private final String operation;

    // ── Constructors ──────────────────────────────────────────────────

    public DatabaseException(String operation, String message) {
        super("[DB:" + operation + "] " + message);
        this.operation = operation;
    }

    public DatabaseException(String operation, String message, Throwable cause) {
        super("[DB:" + operation + "] " + message, cause);
        this.operation = operation;
    }

    // ── Getter ────────────────────────────────────────────────────────

    /**
     * Returns the name of the DAO operation that triggered this exception.
     * Useful for logging or UI error messages.
     */
    public String getOperation() {
        return operation;
    }
}
