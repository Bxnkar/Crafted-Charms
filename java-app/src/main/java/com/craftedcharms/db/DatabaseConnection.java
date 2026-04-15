package com.craftedcharms.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.craftedcharms.exception.DatabaseException;

/**
 * Singleton database connection manager — MySQL (JDBC).
 *
 * Credentials are loaded from src/main/resources/config.properties
 * so they are NEVER hardcoded in source code.
 *
 * Fix for:
 * ⚠1 — Hardcoded DB_USER / DB_PASS replaced with config.properties.
 * ⚠3 — Throws DatabaseException instead of bare RuntimeException.
 *
 * ── How to configure ──────────────────────────────────────────────────
 * Prefer environment variables in production:
 *   DB_URL, DB_USER, DB_PASSWORD
 * Otherwise set src/main/resources/config.properties:
 *   db.url, db.user, db.password
 * ──────────────────────────────────────────────────────────────────────
 */
public class DatabaseConnection {

    // ── Config keys ───────────────────────────────────────────────────
    private static final String CONFIG_FILE = "/config.properties";
    private static final String KEY_URL = "db.url";
    private static final String KEY_USER = "db.user";
    private static final String KEY_PASS = "db.password";

    // ── Singleton state ───────────────────────────────────────────────
    private static DatabaseConnection instance;
    private Connection connection;

    // Credentials loaded once from config file
    private static String dbUrl;
    private static String dbUser;
    private static String dbPass;

    static {
        loadConfig();
    }

    // ── Private constructor ──────────────────────────────────────────

    /**
     * Private constructor — prevents external instantiation (Singleton pattern).
     *
     * @throws SQLException if JDBC connection cannot be established.
     */
    private DatabaseConnection() throws SQLException {
        this.connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
        System.out.println("[DB] Connected to MySQL — crafted_charms database.");
    }

    // ── Static factory ────────────────────────────────────────────────

    /**
     * Returns the single DatabaseConnection instance.
     * Reconnects automatically if the connection was closed or dropped.
     *
     * @return the active DatabaseConnection singleton.
     * @throws DatabaseException if a new connection cannot be established.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (isClosed()) {
            try {
                instance = new DatabaseConnection();
            } catch (SQLException e) {
                throw new DatabaseException(
                        "getInstance",
                        "Cannot connect to MySQL database.\n" +
                                "  • Make sure MySQL is running.\n" +
                                "  • Check credentials in src/main/resources/config.properties\n" +
                                "  Error: " + e.getMessage(),
                        e);
            }
        }
        return instance;
    }

    // ── Public accessors ──────────────────────────────────────────────

    /**
     * Returns the raw JDBC {@link Connection}.
     * All DAO classes call this to obtain the shared connection.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Gracefully closes the underlying JDBC connection.
     * Called once from {@code MainMenu.start()} at application exit.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }

    // ── Private helpers ───────────────────────────────────────────────

    /**
     * Loads database credentials from config.properties on the classpath.
     * Called once in the static initialiser block.
     *
     * @throws DatabaseException if the file is missing or malformed.
     */
    private static void loadConfig() {
        String envUrl  = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");
        if (isNonBlank(envUrl) && isNonBlank(envUser) && envPass != null) {
            dbUrl = envUrl.trim();
            dbUser = envUser.trim();
            dbPass = envPass;
            return;
        }

        try (InputStream in = DatabaseConnection.class.getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new DatabaseException(
                        "loadConfig",
                        "No database configuration found. " +
                                "Set environment variables DB_URL/DB_USER/DB_PASSWORD " +
                                "or create src/main/resources/config.properties.");
            }
            Properties props = new Properties();
            props.load(in);
            dbUrl = props.getProperty(KEY_URL);
            dbUser = props.getProperty(KEY_USER);
            dbPass = props.getProperty(KEY_PASS);

            if (dbUrl == null || dbUser == null || dbPass == null) {
                throw new DatabaseException(
                        "loadConfig",
                        "config.properties is missing one or more keys: " +
                                KEY_URL + ", " + KEY_USER + ", " + KEY_PASS);
            }
            dbUrl = dbUrl.trim();
            dbUser = dbUser.trim();
        } catch (IOException e) {
            throw new DatabaseException("loadConfig",
                    "Failed to read config.properties: " + e.getMessage(), e);
        }
    }

    private static boolean isNonBlank(String value) {
        return value != null && !value.isBlank();
    }

    /** Returns true if no connection exists or it has been closed. */
    private static boolean isClosed() {
        try {
            return instance == null
                    || instance.connection == null
                    || instance.connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }
}
