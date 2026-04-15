-- ======================================================================
--  Crafted Charms — Database Schema (MySQL)
--  Run this file ONCE to create the database and all tables.
--
--  Usage:  mysql -u root -p < sql/schema.sql
--
--  Changes from v1:
--    • Added extra_attribute column to products table
--      → Persists Ring.ringSize / Necklace.length / Bracelet.wristSize
--      → Fix ⚠5: Subclass attributes now stored in MySQL
-- ======================================================================

CREATE DATABASE IF NOT EXISTS crafted_charms
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE crafted_charms;

-- ─────────────────────────────────────────────────────────────────────
--  USERS — login accounts with role-based access
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    user_id       INT            AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)    NOT NULL UNIQUE,
    password_hash VARCHAR(255)   NOT NULL,          -- PBKDF2 storage format (legacy SHA-256 supported)
    role          ENUM('ADMIN', 'CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
    full_name     VARCHAR(100)   NOT NULL,
    email         VARCHAR(150)   NOT NULL UNIQUE,
    created_at    TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────────────────────────────────
--  CUSTOMERS — customer shipping / contact profiles
--  One-to-one with users (CUSTOMER role)
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT          AUTO_INCREMENT PRIMARY KEY,
    user_id     INT          NOT NULL,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL,
    phone       VARCHAR(15)  NOT NULL,
    address     TEXT         NOT NULL,
    city        VARCHAR(60)  NOT NULL,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ─────────────────────────────────────────────────────────────────────
--  PRODUCTS — jewellery inventory
--
--  extra_attribute stores category-specific size/length field:
--    RING     → ring size   (e.g. "US 7")
--    NECKLACE → chain length (e.g. "18 inches")
--    BRACELET → wrist size  (e.g. "M")
--    EARRING  → NULL
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS products (
    product_id      INT            AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150)   NOT NULL,
    description     TEXT,
    category        ENUM('RING', 'NECKLACE', 'BRACELET', 'EARRING') NOT NULL,
    material        VARCHAR(100)   NOT NULL,
    gemstone        VARCHAR(100),
    price           DECIMAL(10,2)  NOT NULL,
    stock_qty       INT            NOT NULL DEFAULT 0,
    is_active       TINYINT(1)     NOT NULL DEFAULT 1,
    extra_attribute VARCHAR(100)   DEFAULT NULL,   -- ring_size / chain_length / wrist_size
    created_at      TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_price    CHECK (price     >  0),
    CONSTRAINT chk_stock    CHECK (stock_qty >= 0)
);

-- ─────────────────────────────────────────────────────────────────────
--  ORDERS — customer purchase orders
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS orders (
    order_id         INT            AUTO_INCREMENT PRIMARY KEY,
    customer_id      INT            NOT NULL,
    order_date       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    status           ENUM('PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED')
                     NOT NULL DEFAULT 'PENDING',
    total_amount     DECIMAL(12,2)  NOT NULL,
    shipping_address TEXT           NOT NULL,
    payment_method   VARCHAR(50)    NOT NULL,
    FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- ─────────────────────────────────────────────────────────────────────
--  ORDER_ITEMS — line items within an order
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS order_items (
    item_id      INT            AUTO_INCREMENT PRIMARY KEY,
    order_id     INT            NOT NULL,
    product_id   INT            NOT NULL,
    product_name VARCHAR(150)   NOT NULL,          -- snapshot at purchase time
    quantity     INT            NOT NULL,
    unit_price   DECIMAL(10,2)  NOT NULL,
    subtotal     DECIMAL(12,2)  NOT NULL,
    CONSTRAINT chk_qty CHECK (quantity > 0),
    FOREIGN KEY (order_id)
        REFERENCES orders(order_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (product_id)
        REFERENCES products(product_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- ─────────────────────────────────────────────────────────────────────
--  INDEXES — for query performance
-- ─────────────────────────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_products_category  ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_active    ON products(is_active);
CREATE INDEX IF NOT EXISTS idx_products_name      ON products(name);
CREATE INDEX IF NOT EXISTS idx_orders_customer    ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status      ON orders(status);
CREATE INDEX IF NOT EXISTS idx_customers_user     ON customers(user_id);
CREATE INDEX IF NOT EXISTS idx_customers_city     ON customers(city);

-- ─────────────────────────────────────────────────────────────────────
--  MIGRATION — run only if upgrading an existing database (v1 → v2)
--  Skip this block if running schema fresh.
-- ─────────────────────────────────────────────────────────────────────
-- ALTER TABLE products ADD COLUMN IF NOT EXISTS
--     extra_attribute VARCHAR(100) DEFAULT NULL
--     AFTER is_active;

-- ======================================================================
--  Schema created successfully.
--  Next step: run sql/sample_data.sql to seed demo records.
-- ======================================================================
