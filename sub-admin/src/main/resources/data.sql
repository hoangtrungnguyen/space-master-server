-- /Users/trungnguyenhoang/IdeaProjects/server/sub-admin/src/main/resources/data.sql


-- Insert Users
-- Passwords should be properly hashed in a real application (e.g., with BCrypt)
INSERT INTO public.users (id, username, password_hash, role)
VALUES ('b2c3d4e5-f6a7-8901-2345-67890abcdef0', 'cashier', '$2a$10$e.ExV5CM13Oq.f4e.sK.FeT0A0d5iC9.a.G.a.G.a.G.a',
        'STAFF');

-- Insert Brands
INSERT INTO public.brands (id, name, created_at, updated_at)
VALUES ('c3d4e5f6-a7b8-9012-3456-7890abcdef01', 'Quantum Gadgets', now(), now()),
       ('d4e5f6a7-b8c9-0123-4567-890abcdef012', 'Stellar Stationery', now(), now());

-- Insert Categories
INSERT INTO public.categories (id, name, parent_category_id, created_at, updated_at)
VALUES ('e5f6a7b8-c9d0-1234-5678-90abcdef0123', 'Electronics', NULL, now(), now()),
       ('f6a7b8c9-d0e1-2345-6789-0abcdef01234', 'Laptops', 'e5f6a7b8-c9d0-1234-5678-90abcdef0123', now(), now()),
       ('a7b8c9d0-e1f2-3456-7890-bcdef0123456', 'Office Supplies', NULL, now(), now());

-- Insert Products
INSERT INTO public.products (id, name, description, brand_id, category_id, is_active, created_at, updated_at)
VALUES (1, 'QuantumBook Pro', 'A powerful and sleek laptop for professionals.', 'c3d4e5f6-a7b8-9012-3456-7890abcdef01',
        'f6a7b8c9-d0e1-2345-6789-0abcdef01234', true, now(), now()),
       (2, 'Stellar Gel Pen', 'A smooth writing gel pen.', 'd4e5f6a7-b8c9-0123-4567-890abcdef012',
        'a7b8c9d0-e1f2-3456-7890-bcdef0123456', true, now(), now());