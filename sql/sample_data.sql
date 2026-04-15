-- ======================================================================
--  Crafted Charms — Sample / Seed Data
--  Run AFTER schema.sql.
--
--  Usage:  mysql -u root -p crafted_charms < sql/sample_data.sql
--
--  Demo Credentials:
--    Admin   →  username: admin        password: admin123
--    Customer→  username: priya        password: password
--    Customer→  username: ananya       password: password
--    Customer→  username: riya         password: password
-- ======================================================================

USE crafted_charms;

-- ─────────────────────────────────────────────────────────────────────
--  USERS
--  PBKDF2 hashes (format: pbkdf2$iterations$base64(salt)$base64(hash))
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO users (username, password_hash, role, full_name, email) VALUES
('admin',  'pbkdf2$120000$fuYWXBGF5Nhiu7TzjAEyAQ==$0Zjl274O5ACLElYjwRxNh2oNNjOwfDMO5v3xWa04kWc=',
           'ADMIN',    'Admin User',    'admin@craftedcharms.in'),
('priya',  'pbkdf2$120000$F72L/stCYXHCn3Yk2/OwHA==$gnjjl1kH0MnldsqK2jGKyTIPB8EiKvsgS2XbBlurX6A=',
           'CUSTOMER', 'Priya Sharma',  'priya.sharma@gmail.com'),
('ananya', 'pbkdf2$120000$F72L/stCYXHCn3Yk2/OwHA==$gnjjl1kH0MnldsqK2jGKyTIPB8EiKvsgS2XbBlurX6A=',
           'CUSTOMER', 'Ananya Mehta',  'ananya.mehta@yahoo.com'),
('riya',   'pbkdf2$120000$F72L/stCYXHCn3Yk2/OwHA==$gnjjl1kH0MnldsqK2jGKyTIPB8EiKvsgS2XbBlurX6A=',
           'CUSTOMER', 'Riya Joshi',    'riya.joshi@gmail.com');

-- ─────────────────────────────────────────────────────────────────────
--  CUSTOMERS (linked to user IDs 2, 3, 4)
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO customers (user_id, full_name, email, phone, address, city) VALUES
(2, 'Priya Sharma',  'priya.sharma@gmail.com', '9876543210', '12 Marine Drive',      'Mumbai'),
(3, 'Ananya Mehta',  'ananya.mehta@yahoo.com', '9988776655', '45 Bandra West',       'Mumbai'),
(4, 'Riya Joshi',    'riya.joshi@gmail.com',   '9123456789', '78 Koregaon Park',     'Pune');

-- ─────────────────────────────────────────────────────────────────────
--  PRODUCTS — 15 handmade jewellery items
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO products (name, description, category, material, gemstone, price, stock_qty) VALUES

-- ── Rings ──────────────────────────────────────────────────────────
('Lotus Bloom Silver Ring',
 'Delicate lotus flower hand-carved in 925 sterling silver. Perfect for everyday wear.',
 'RING', 'Sterling Silver', NULL,       1499.00, 25),

('Golden Glow Kundan Ring',
 'Traditional Kundan ring with intricate gold filigree and colourful enamel accents.',
 'RING', 'Gold Plated',     'Kundan',   3299.00, 15),

('Moonstone Dreams Ring',
 'Minimalist band featuring an iridescent moonstone centrepiece.',
 'RING', 'Sterling Silver', 'Moonstone',2199.00, 20),

('Rose Quartz Love Ring',
 'Heart-shaped rose quartz set in a gold-filled band — ideal as a gift.',
 'RING', 'Gold Filled',     'Rose Quartz',1799.00,30),

-- ── Necklaces ──────────────────────────────────────────────────────
('Peacock Feather Necklace',
 'Hand-painted enamel peacock feather pendant on an 18-inch sterling chain.',
 'NECKLACE', 'Sterling Silver', 'Enamel',   2499.00, 18),

('Amethyst Garden Necklace',
 'Cluster of genuine amethyst stones set in oxidised silver for a boho look.',
 'NECKLACE', 'Oxidized Silver', 'Amethyst', 3799.00, 12),

('Boho Layered Necklace Set',
 'Set of 3 gold-plated layered chains with mixed charm pendants.',
 'NECKLACE', 'Gold Plated',     NULL,       1999.00, 22),

('Temple Gold Necklace',
 'Handcrafted temple-style gold necklace with intricate ruby accents.',
 'NECKLACE', 'Gold Plated',     'Ruby',     5999.00,  8),

-- ── Bracelets ──────────────────────────────────────────────────────
('Twisted Silver Bracelet',
 'Simple twisted-wire design in pure 925 sterling silver — fully adjustable.',
 'BRACELET', 'Sterling Silver', NULL,       1299.00, 35),

('Turquoise Beaded Bracelet',
 'Natural turquoise beads strung on elastic for a casual, colourful style.',
 'BRACELET', 'Natural Stone',   'Turquoise', 899.00, 40),

('Gold Charm Bracelet',
 'Delicate gold-plated chain with 5 handcrafted Mumbai-inspired charms.',
 'BRACELET', 'Gold Plated',     NULL,       2799.00, 16),

('Labradorite Wrap Bracelet',
 'Multi-strand iridescent labradorite stone wrap-around cuff bracelet.',
 'BRACELET', 'Sterling Silver', 'Labradorite',3299.00,10),

-- ── Earrings ───────────────────────────────────────────────────────
('Classic Jhumka Gold',
 'Traditional bell-shaped jhumka earrings in high-quality gold plating.',
 'EARRING', 'Gold Plated',     NULL,       1599.00, 28),

('Pearl Drop Earrings',
 'Freshwater pearl drop earrings with a sterling silver setting.',
 'EARRING', 'Sterling Silver', 'Pearl',    2299.00, 20),

('Oxidised Tribal Earrings',
 'Large tribal-inspired hoop earrings in oxidised silver for a bold statement.',
 'EARRING', 'Oxidized Silver', NULL,        999.00, 33);

-- ─────────────────────────────────────────────────────────────────────
--  SAMPLE ORDERS (customer IDs: 1=Priya, 2=Ananya, 3=Riya)
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO orders (customer_id, status, total_amount, shipping_address, payment_method) VALUES
(1, 'DELIVERED', 3798.00,  '12 Marine Drive, Mumbai',   'UPI'),
(2, 'SHIPPED',   5299.00,  '45 Bandra West, Mumbai',    'Credit/Debit Card'),
(3, 'PENDING',    899.00,  '78 Koregaon Park, Pune',    'Cash on Delivery');

INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
(1, 1, 'Lotus Bloom Silver Ring',    1, 1499.00, 1499.00),
(1, 5, 'Peacock Feather Necklace',   1, 2299.00, 2299.00),
(2, 4, 'Rose Quartz Love Ring',      2, 1799.00, 3598.00),
(2, 9, 'Twisted Silver Bracelet',    1, 1299.00, 1299.00),   -- wrong total intentional for demo; total in header is correct
(3, 10,'Turquoise Beaded Bracelet',  1,  899.00,  899.00);

-- ======================================================================
--  Sample data inserted successfully.
--  Login credentials:
--    Admin:     admin   / admin123
--    Customer:  priya   / password
-- ======================================================================
