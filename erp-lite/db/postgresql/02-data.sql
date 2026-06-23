-- =============================================================================
-- ERP LITE - INITIAL DATA
-- Version: 2.0
-- Purpose: Test data for development and testing
-- =============================================================================

-- =============================================================================
-- PRODUCTS (20 records)
-- =============================================================================
INSERT INTO products (id, sku, name, description, price, stock, category_id, active) VALUES
('11111111-1111-1111-1111-111111111111', 'LAPTOP-001', 'Laptop Dell XPS 15', 'High-performance laptop with Intel i7, 16GB RAM, 512GB SSD', 1499.99, 25, 'cat-electronics', TRUE),
('22222222-2222-2222-2222-222222222222', 'LAPTOP-002', 'MacBook Pro 14"', 'Apple M3 Pro chip, 18GB RAM, 512GB SSD', 1999.99, 15, 'cat-electronics', TRUE),
('33333333-3333-3333-3333-333333333333', 'DESK-001', 'Standing Desk Electric', 'Adjustable height standing desk, 120x60cm', 499.99, 30, 'cat-furniture', TRUE),
('44444444-4444-4444-4444-444444444444', 'CHAIR-001', 'Ergonomic Office Chair', 'Herman Miller Aeron, size B, fully adjustable', 1299.99, 12, 'cat-furniture', TRUE),
('55555555-5555-5555-5555-555555555555', 'MONITOR-001', 'Dell UltraSharp 27"', '4K IPS monitor, USB-C, 99% sRGB', 599.99, 40, 'cat-electronics', TRUE),
('66666666-6666-6666-6666-666666666666', 'KEYBOARD-001', 'Mechanical Keyboard RGB', 'Cherry MX Brown switches, aluminum frame', 149.99, 50, 'cat-electronics', TRUE),
('77777777-7777-7777-7777-777777777777', 'MOUSE-001', 'Logitech MX Master 3S', 'Wireless mouse, ergonomic, 8K DPI', 99.99, 60, 'cat-electronics', TRUE),
('88888888-8888-8888-8888-888888888888', 'PRINTER-001', 'HP LaserJet Pro', 'Black & white laser printer, duplex, Wi-Fi', 299.99, 20, 'cat-electronics', TRUE),
('99999999-9999-9999-9999-999999999999', 'TABLET-001', 'iPad Air 11"', 'M2 chip, 128GB, Wi-Fi, Space Gray', 599.99, 35, 'cat-electronics', TRUE),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'LAMP-001', 'LED Desk Lamp', 'Adjustable brightness, USB charging port', 49.99, 80, 'cat-furniture', TRUE),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'WEBCAM-001', 'Logitech C920 HD Pro', '1080p video, stereo audio, autofocus', 79.99, 45, 'cat-electronics', TRUE),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'HEADSET-001', 'Sony WH-1000XM5', 'Noise cancelling, 30h battery, Bluetooth', 399.99, 28, 'cat-electronics', TRUE),
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'DOCK-001', 'CalDigit TS4', 'Thunderbolt 4 dock, 18 ports, 98W power delivery', 399.99, 18, 'cat-electronics', TRUE),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'CABLE-001', 'USB-C Cable 2m', 'Thunderbolt 4, 40Gbps, 100W charging', 29.99, 100, 'cat-electronics', TRUE),
('ffffffff-ffff-ffff-ffff-ffffffffffff', 'BACKPACK-001', 'Laptop Backpack', 'Water-resistant, fits 15" laptop, USB charging port', 69.99, 55, 'cat-accessories', TRUE),
('10101010-1010-1010-1010-101010101010', 'ORGANIZER-001', 'Desktop Organizer', 'Bamboo wood, 5 compartments, cable management', 39.99, 70, 'cat-furniture', TRUE),
('20202020-2020-2020-2020-202020202020', 'PLANT-001', 'Artificial Desk Plant', 'Small succulent, realistic, no maintenance', 19.99, 90, 'cat-accessories', TRUE),
('30303030-3030-3030-3030-303030303030', 'NOTEBOOK-001', 'Moleskine Classic Notebook', 'Large, ruled, hardcover, 240 pages', 24.99, 75, 'cat-stationery', TRUE),
('40404040-4040-4040-4040-404040404040', 'PEN-001', 'Pilot G2 Gel Pens Pack', 'Pack of 12, 0.7mm, black ink', 14.99, 120, 'cat-stationery', TRUE),
('50505050-5050-5050-5050-505050505050', 'WHITEBOARD-001', 'Magnetic Whiteboard', '90x60cm, aluminum frame, includes markers', 89.99, 22, 'cat-furniture', TRUE);

-- =============================================================================
-- ORDERS (15 records)
-- =============================================================================
INSERT INTO orders (id, order_number, customer_id, customer_name, created_by, order_date, status, total_amount, currency) VALUES
('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 'ORD-2025-001', 1, 'Leanne Graham', 'admin',    '2025-01-15 10:30:00', 'DELIVERED', 2149.97, 'USD'),
('a2a2a2a2-a2a2-a2a2-a2a2-a2a2a2a2a2a2', 'ORD-2025-002', 2, 'Ervin Howell',  'manager',  '2025-01-18 14:20:00', 'SHIPPED',   1799.98, 'USD'),
('a3a3a3a3-a3a3-a3a3-a3a3-a3a3a3a3a3a3', 'ORD-2025-003', 3, 'Clementine Bauch', 'employee', '2025-01-20 09:15:00', 'CONFIRMED',  899.98, 'USD'),
('a4a4a4a4-a4a4-a4a4-a4a4-a4a4a4a4a4a4', 'ORD-2025-004', 4, 'Patricia Lebsack', 'admin',   '2025-01-22 16:45:00', 'PENDING',   3299.96, 'USD'),
('a5a5a5a5-a5a5-a5a5-a5a5-a5a5a5a5a5a5', 'ORD-2025-005', 5, 'Chelsey Dietrich', 'manager', '2025-01-25 11:00:00', 'DELIVERED',  679.98, 'USD'),
('a6a6a6a6-a6a6-a6a6-a6a6-a6a6a6a6a6a6', 'ORD-2025-006', 6, 'Mrs. Dennis Schulist', 'employee', '2025-01-28 13:30:00', 'CONFIRMED', 1549.97, 'USD'),
('a7a7a7a7-a7a7-a7a7-a7a7-a7a7a7a7a7a7', 'ORD-2025-007', 7, 'Kurtis Weissnat', 'admin',   '2025-02-01 10:00:00', 'SHIPPED',    249.96, 'USD'),
('a8a8a8a8-a8a8-a8a8-a8a8-a8a8a8a8a8a8', 'ORD-2025-008', 8, 'Nicholas Runolfsdottir', 'manager', '2025-02-03 15:20:00', 'DELIVERED', 1999.99, 'USD'),
('a9a9a9a9-a9a9-a9a9-a9a9-a9a9a9a9a9a9', 'ORD-2025-009', 9, 'Glenna Reichert', 'employee', '2025-02-05 09:45:00', 'PENDING',    499.99, 'USD'),
('b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1', 'ORD-2025-010', 10, 'Clementina DuBuque', 'admin', '2025-02-08 14:10:00', 'CONFIRMED', 2099.95, 'USD'),
('b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', 'ORD-2025-011', 1, 'Leanne Graham', 'manager',  '2025-02-10 11:30:00', 'SHIPPED',    129.98, 'USD'),
('b3b3b3b3-b3b3-b3b3-b3b3-b3b3b3b3b3b3', 'ORD-2025-012', 2, 'Ervin Howell',  'employee', '2025-02-12 16:00:00', 'DELIVERED',   89.99, 'USD'),
('b4b4b4b4-b4b4-b4b4-b4b4-b4b4b4b4b4b4', 'ORD-2025-013', 3, 'Clementine Bauch', 'admin', '2025-02-15 10:20:00', 'PENDING',   1899.97, 'USD'),
('b5b5b5b5-b5b5-b5b5-b5b5-b5b5b5b5b5b5', 'ORD-2025-014', 4, 'Patricia Lebsack', 'manager', '2025-02-18 13:50:00', 'CONFIRMED',  479.97, 'USD'),
('b6b6b6b6-b6b6-b6b6-b6b6-b6b6b6b6b6b6', 'ORD-2025-015', 5, 'Chelsey Dietrich', 'employee', '2025-02-20 09:30:00', 'SHIPPED',   749.97, 'USD');

-- =============================================================================
-- ORDER_PRODUCTS (Order lines for 15 orders)
-- =============================================================================

-- ORD-2025-001 (3 items)
INSERT INTO order_products (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', '11111111-1111-1111-1111-111111111111', 'Laptop Dell XPS 15',         1, 1499.99, 1499.99),
('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', '66666666-6666-6666-6666-666666666666', 'Mechanical Keyboard RGB',    2,  149.99,  299.98),
('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', '77777777-7777-7777-7777-777777777777', 'Logitech MX Master 3S',      3,   99.99,  299.97);

-- ORD-2025-002 (2 items)
INSERT INTO order_products (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
('a2a2a2a2-a2a2-a2a2-a2a2-a2a2a2a2a2a2', '22222222-2222-2222-2222-222222222222', 'MacBook Pro 14"',            1, 1999.99, 1999.99),
('a2a2a2a2-a2a2-a2a2-a2a2-a2a2a2a2a2a2', '77777777-7777-7777-7777-777777777777', 'Logitech MX Master 3S',      2,   99.99,  199.98);

-- ORD-2025-003 (3 items)
INSERT INTO order_products (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
('a3a3a3a3-a3a3-a3a3-a3a3-a3a3a3a3a3a3', '55555555-5555-5555-5555-555555555555', 'Dell UltraSharp 27"',        1,  599.99,  599.99),
('a3a3a3a3-a3a3-a3a3-a3a3-a3a3a3a3a3a3', '66666666-6666-6666-6666-666666666666', 'Mechanical Keyboard RGB',    1,  149.99,  149.99),
('a3a3a3a3-a3a3-a3a3-a3a3-a3a3a3a3a3a3', '77777777-7777-7777-7777-777777777777', 'Logitech MX Master 3S',      1,   99.99,   99.99);

-- ORD-2025-004 (2 items)
INSERT INTO order_products (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
('a4a4a4a4-a4a4-a4a4-a4a4-a4a4a4a4a4a4', '22222222-2222-2222-2222-222222222222', 'MacBook Pro 14"',            1, 1999.99, 1999.99),
('a4a4a4a4-a4a4-a4a4-a4a4-a4a4a4a4a4a4', '44444444-4444-4444-4444-444444444444', 'Ergonomic Office Chair',     1, 1299.99, 1299.99);

-- ORD-2025-005 (4 items)
INSERT INTO order_products (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
('a5a5a5a5-a5a5-a5a5-a5a5-a5a5a5a5a5a5', '66666666-6666-6666-6666-666666666666', 'Mechanical Keyboard RGB',    2,  149.99,  299.98),
('a5a5a5a5-a5a5-a5a5-a5a5-a5a5a5a5a5a5', '77777777-7777-7777-7777-777777777777', 'Logitech MX Master 3S',      2,   99.99,  199.98),
('a5a5a5a5-a5a5-a5a5-a5a5-a5a5a5a5a5a5', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Logitech C920 HD Pro',      1,   79.99,   79.99),
('a5a5a5a5-a5a5-a5a5-a5a5-a5a5a5a5a5a5', 'ffffffff-ffff-ffff-ffff-ffffffffffff', 'Laptop Backpack',            1,   69.99,   69.99);

-- ORD-2025-006 (3 items)
INSERT INTO order_products (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
('a6a6a6a6-a6a6-a6a6-a6a6-a6a6a6a6a6a6', '11111111-1111-1111-1111-111111111111', 'Laptop Dell XPS 15',         1, 1499.99, 1499.99),
('a6a6a6a6-a6a6-a6a6-a6a6-a6a6a6a6a6a6', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'LED Desk Lamp',              1,   49.99,   49.99),
('a6a6a6a6-a6a6-a6a6-a6a6-a6a6a6a6a6a6', '77777777-7777-7777-7777-777777777777', 'Logitech MX Master 3S',      1,   99.99,   99.99);

-- ORD-2025-007 (5 items)
INSERT INTO order_products (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
('a7a7a7a7-a7a7-a7a7-a7a7-a7a7a7a7a7a7', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'LED Desk Lamp',              2,   49.99,   99.98),
('a7a7a7a7-a7a7-a7a7-a7a7-a7a7a7a7a7a7', '20202020-2020-2020-2020-202020202020', 'Artificial Desk Plant',      3,   19.99,   59.97),
('a7a7a7a7-a7a7-a7a7-a7a7-a7a7a7a7a7a7', '30303030-3030-3030-3030-303030303030', 'Moleskine Classic Notebook', 2,   24.99,   49.98),
('a7a7a7a7-a7a7-a7a7-a7a7-a7a7a7a7a7a7', '40404040-4040-4040-4040-404040404040', 'Pilot G2 Gel Pens Pack',     2,   14.99,   29.98),
('a7a7a7a7-a7a7-a7a7-a7a7-a7a7a7a7a7a7', '10101010-1010-1010-1010-101010101010', 'Desktop Organizer',          1,   39.99,   39.99);

-- ORD-2025-008 (1 item)
INSERT INTO order_products (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
('a8a8a8a8-a8a8-a8a8-a8a8-a8a8a8a8a8a8', '22222222-2222-2222-2222-222222222222', 'MacBook Pro 14"',            1, 1999.99, 1999.99);

-- ORD-2025-009 (1 item)
INSERT INTO order_products (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
('a9a9a9a9-a9a9-a9a9-a9a9-a9a9a9a9a9a9', '33333333-3333-3333-3333-333333333333', 'Standing Desk Electric',     1,  499.99,  499.99);

-- ORD-2025-010 (4 items)
INSERT INTO order_products (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
('b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1', '11111111-1111-1111-1111-111111111111', 'Laptop Dell XPS 15',         1, 1499.99, 1499.99),
('b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1', '55555555-5555-5555-5555-555555555555', 'Dell UltraSharp 27"',        1,  599.99,  599.99),
('b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1', '66666666-6666-6666-6666-666666666666', 'Mechanical Keyboard RGB',    1,  149.99,  149.99),
('b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1', '77777777-7777-7777-7777-777777777777', 'Logitech MX Master 3S',      1,   99.99,   99.99);

-- ORD-2025-011 (2 items)
INSERT INTO order_products (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
('b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Logitech C920 HD Pro',      1,   79.99,   79.99),
('b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'LED Desk Lamp',              1,   49.99,   49.99);

-- ORD-2025-012 (1 item)
INSERT INTO order_products (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
('b3b3b3b3-b3b3-b3b3-b3b3-b3b3b3b3b3b3', '50505050-5050-5050-5050-505050505050', 'Magnetic Whiteboard',       1,   89.99,   89.99);

-- ORD-2025-013 (3 items)
INSERT INTO order_products (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
('b4b4b4b4-b4b4-b4b4-b4b4-b4b4b4b4b4b4', '44444444-4444-4444-4444-444444444444', 'Ergonomic Office Chair',     1, 1299.99, 1299.99),
('b4b4b4b4-b4b4-b4b4-b4b4-b4b4b4b4b4b4', '55555555-5555-5555-5555-555555555555', 'Dell UltraSharp 27"',        1,  599.99,  599.99),
('b4b4b4b4-b4b4-b4b4-b4b4-b4b4b4b4b4b4', '77777777-7777-7777-7777-777777777777', 'Logitech MX Master 3S',      1,   99.99,   99.99);

-- ORD-2025-014 (3 items)
INSERT INTO order_products (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
('b5b5b5b5-b5b5-b5b5-b5b5-b5b5b5b5b5b5', '66666666-6666-6666-6666-666666666666', 'Mechanical Keyboard RGB',    2,  149.99,  299.98),
('b5b5b5b5-b5b5-b5b5-b5b5-b5b5b5b5b5b5', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Logitech C920 HD Pro',      1,   79.99,   79.99),
('b5b5b5b5-b5b5-b5b5-b5b5-b5b5b5b5b5b5', 'ffffffff-ffff-ffff-ffff-ffffffffffff', 'Laptop Backpack',            1,   69.99,   69.99);

-- ORD-2025-015 (3 items)
INSERT INTO order_products (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
('b6b6b6b6-b6b6-b6b6-b6b6-b6b6b6b6b6b6', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'Sony WH-1000XM5',            1,  399.99,  399.99),
('b6b6b6b6-b6b6-b6b6-b6b6-b6b6b6b6b6b6', '66666666-6666-6666-6666-666666666666', 'Mechanical Keyboard RGB',    1,  149.99,  149.99),
('b6b6b6b6-b6b6-b6b6-b6b6-b6b6b6b6b6b6', '77777777-7777-7777-7777-777777777777', 'Logitech MX Master 3S',      2,   99.99,  199.98);