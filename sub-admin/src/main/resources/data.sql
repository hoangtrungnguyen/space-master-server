-- Note: This script assumes the existence of the tables from your schema.
-- It's generally a good practice to run this in a transaction.

BEGIN;

-- 1. Users
-- Passwords should be properly hashed in a real application. These are placeholders.
INSERT INTO users (id, uuid, username, password_hash, role) VALUES
                                                                (1, gen_random_uuid(), 'admin_user', '$2a$10$m.aA5V.bV/i7NqE5V.J5y.N/E5V.J5y.N/E5V.J5y.N/E5V.J5y.N', 'ADMIN'),
                                                                (2, gen_random_uuid(), 'manager_sam', '$2a$10$m.aA5V.bV/i7NqE5V.J5y.N/E5V.J5y.N/E5V.J5y.N/E5V.J5y.N', 'MANAGER'),
                                                                (3, gen_random_uuid(), 'staff_jane', '$2a$10$m.aA5V.bV/i7NqE5V.J5y.N/E5V.J5y.N/E5V.J5y.N/E5V.J5y.N', 'STAFF');

-- 2. Brands
-- Using md5(random()::text)::bytea for bytea IDs as a simple way to generate unique byte arrays.
INSERT INTO brands (id, name, created_at, updated_at) VALUES
                                                          (md5(random()::text)::bytea, 'Apple', NOW(), NOW()),
                                                          (md5(random()::text)::bytea, 'Samsung', NOW(), NOW()),
                                                          (md5(random()::text)::bytea, 'Logitech', NOW(), NOW()),
                                                          (md5(random()::text)::bytea, 'Sony', NOW(), NOW());

-- 3. Categories
-- Create parent categories first
INSERT INTO categories (id, name, parent_category_id, created_at, updated_at) VALUES
                                                                                  (md5('Electronics')::bytea, 'Electronics', NULL, NOW(), NOW()),
                                                                                  (md5('Computers')::bytea, 'Computers', NULL, NOW(), NOW()),
                                                                                  (md5('Accessories')::bytea, 'Accessories', NULL, NOW(), NOW());

-- Create child categories
INSERT INTO categories (id, name, parent_category_id, created_at, updated_at) VALUES
                                                                                  (md5('Smartphones')::bytea, 'Smartphones', (SELECT id FROM categories WHERE name = 'Electronics'), NOW(), NOW()),
                                                                                  (md5('Laptops')::bytea, 'Laptops', (SELECT id FROM categories WHERE name = 'Computers'), NOW(), NOW()),
                                                                                  (md5('Keyboards')::bytea, 'Keyboards', (SELECT id FROM categories WHERE name = 'Accessories'), NOW(), NOW()),
                                                                                  (md5('Headphones')::bytea, 'Headphones', (SELECT id FROM categories WHERE name = 'Accessories'), NOW(), NOW());


-- 4. Customers
INSERT INTO customers (id, uuid, first_name, last_name, email, phone_number, created_at, updated_at) VALUES
                                                                                                         (1, gen_random_uuid(), 'John', 'Doe', 'john.doe@example.com', '123-456-7890', NOW(), NOW()),
                                                                                                         (2, gen_random_uuid(), 'Alice', 'Smith', 'alice.smith@example.com', '098-765-4321', NOW(), NOW());

-- 5. Products
-- Link products to users, brands, and categories
INSERT INTO products (id, name, description, is_active, brand_id, category_id, created_by_user_id, created_at, updated_at) VALUES
                                                                                                                               (1, 'iPhone 15 Pro', 'The latest and greatest iPhone.', true, (SELECT id FROM brands WHERE name = 'Apple'), (SELECT id FROM categories WHERE name = 'Smartphones'), 2, NOW(), NOW()),
                                                                                                                               (2, 'Galaxy S24 Ultra', 'Samsung''s flagship with AI features.', true, (SELECT id FROM brands WHERE name = 'Samsung'), (SELECT id FROM categories WHERE name = 'Smartphones'), 2, NOW(), NOW()),
                                                                                                                               (3, 'MacBook Pro 16"', 'Powerful laptop for professionals.', true, (SELECT id FROM brands WHERE name = 'Apple'), (SELECT id FROM categories WHERE name = 'Laptops'), 2, NOW(), NOW()),
                                                                                                                               (4, 'MX Keys S', 'Advanced wireless illuminated keyboard.', true, (SELECT id FROM brands WHERE name = 'Logitech'), (SELECT id FROM categories WHERE name = 'Keyboards'), 3, NOW(), NOW()),
                                                                                                                               (5, 'WH-1000XM5', 'Industry-leading noise canceling headphones.', true, (SELECT id FROM brands WHERE name = 'Sony'), (SELECT id FROM categories WHERE name = 'Headphones'), 3, NOW(), NOW());

-- 6. Product Variants
INSERT INTO product_variants (id, product_id, sku, price, cost_price, weight, attributes, created_at, updated_at) VALUES
                                                                                                                      (1, 1, 'APL-IP15P-256-BLK', 999.00, 750.00, 0.187, '{"color": "Black", "storage": "256GB"}', NOW(), NOW()),
                                                                                                                      (2, 1, 'APL-IP15P-512-BLU', 1199.00, 850.00, 0.187, '{"color": "Blue", "storage": "512GB"}', NOW(), NOW()),
                                                                                                                      (3, 2, 'SAM-S24U-256-GRY', 1299.00, 900.00, 0.232, '{"color": "Titanium Gray", "storage": "256GB"}', NOW(), NOW()),
                                                                                                                      (4, 3, 'APL-MBP16-M3-512', 2499.00, 1800.00, 2.1, '{"chip": "M3 Pro", "storage": "512GB"}', NOW(), NOW()),
                                                                                                                      (5, 4, 'LOG-MXKS-GRAPH', 109.99, 60.00, 0.810, '{"color": "Graphite"}', NOW(), NOW()),
                                                                                                                      (6, 5, 'SON-WHXM5-BLK', 399.99, 250.00, 0.250, '{"color": "Black"}', NOW(), NOW());

-- 7. Inventory
-- One inventory record for each product variant
INSERT INTO inventory (id, product_variant_id, quantity_on_hand, quantity_committed, reorder_level, last_restocked_at) VALUES
                                                                                                                           (gen_random_uuid(), 1, 100, 10, 20, NOW() - INTERVAL '7 day'),
                                                                                                                           (gen_random_uuid(), 2, 50, 5, 10, NOW() - INTERVAL '7 day'),
                                                                                                                           (gen_random_uuid(), 3, 75, 8, 15, NOW() - INTERVAL '5 day'),
                                                                                                                           (gen_random_uuid(), 4, 20, 2, 5, NOW() - INTERVAL '14 day'),
                                                                                                                           (gen_random_uuid(), 5, 150, 25, 30, NOW() - INTERVAL '3 day'),
                                                                                                                           (gen_random_uuid(), 6, 80, 12, 20, NOW() - INTERVAL '3 day');

-- 8. Orders
-- Create a few orders with different statuses
INSERT INTO orders (id, customer_id, status, total_amount, billing_address, shipping_address, order_date, created_at, updated_at) VALUES
                                                                                                                                      (1, 1, 'DELIVERED', 1108.99, '123 Main St, Anytown, USA', '123 Main St, Anytown, USA', NOW() - INTERVAL '10 day', NOW() - INTERVAL '10 day', NOW() - INTERVAL '5 day'),
                                                                                                                                      (2, 2, 'SHIPPED', 1299.00, '456 Oak Ave, Somecity, USA', '456 Oak Ave, Somecity, USA', NOW() - INTERVAL '3 day', NOW() - INTERVAL '3 day', NOW() - INTERVAL '1 day'),
                                                                                                                                      (3, 1, 'PENDING', 399.99, '123 Main St, Anytown, USA', '789 Pine Ln, Otherville, USA', NOW(), NOW(), NOW());

-- 9. Order Items
-- Link order items to orders and product variants
-- Order 1
INSERT INTO order_items (id, order_id, product_variant_id, quantity, price_per_unit, line_total) VALUES
                                                                                                     (1, 1, 1, 1, 999.00, 999.00),
                                                                                                     (2, 1, 5, 1, 109.99, 109.99);

-- Order 2
INSERT INTO order_items (id, order_id, product_variant_id, quantity, price_per_unit, line_total) VALUES
    (3, 2, 3, 1, 1299.00, 1299.00);

-- Order 3
INSERT INTO order_items (id, order_id, product_variant_id, quantity, price_per_unit, line_total) VALUES
    (4, 3, 6, 1, 399.99, 399.99);

-- 10. Payments
-- Link payments to orders
INSERT INTO payments (id, order_id, amount, currency, status, payment_method, metadata, created_at, updated_at) VALUES
                                                                                                                    (gen_random_uuid(), 1, 1108.99, 'USD', 'COMPLETED', 'CARD', '{"transaction_id": "ch_123abc"}', NOW() - INTERVAL '10 day', NOW() - INTERVAL '10 day'),
                                                                                                                    (gen_random_uuid(), 2, 1299.00, 'USD', 'COMPLETED', 'CARD', '{"transaction_id": "ch_456def"}', NOW() - INTERVAL '3 day', NOW() - INTERVAL '3 day'),
                                                                                                                    (gen_random_uuid(), 3, 399.99, 'USD', 'PENDING', 'CASH', NULL, NOW(), NOW());

-- 11. Cash Transactions (example)
INSERT INTO cash_transaction (id, amount, currency, created_at) VALUES
                                                                    (gen_random_uuid(), 50.00, 'USD', NOW() - INTERVAL '1 day'),
                                                                    (gen_random_uuid(), -20.00, 'USD', NOW());

-- 12. Warehouses
INSERT INTO warehouses (id, name, address, created_at, updated_at) VALUES
                                                                       (md5(random()::text)::bytea, 'Main Warehouse', '100 Warehouse Rd, Factoria, USA', NOW(), NOW()),
                                                                       (md5(random()::text)::bytea, 'West Coast Distribution', '200 Logistics Blvd, Shipit, USA', NOW(), NOW());

COMMIT;