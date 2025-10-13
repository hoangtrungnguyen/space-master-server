-- Step 1: Add the new column for the user relationship, allowing NULLs temporarily.
-- We use UUID because the 'users' table's ID is a UUID.
ALTER TABLE products
    ADD COLUMN created_by_user_id UUID;

-- Step 2: Backfill the new column for existing products.
-- We'll find the 'admin' user's ID and set it as the default creator for all
-- products that were created before this change.
-- This is safer than hardcoding a UUID.
UPDATE products
SET created_by_user_id = (SELECT id FROM users WHERE username = 'admin' LIMIT 1)
WHERE created_by_user_id IS NULL;

-- Step 3: Now that all existing rows have a value, enforce the NOT NULL constraint.
ALTER TABLE products
    ALTER COLUMN created_by_user_id SET NOT NULL;

-- Step 4 (Optional but Recommended): Add a foreign key constraint for data integrity.
-- This ensures you can't delete a user who has created products.
ALTER TABLE products
    ADD CONSTRAINT fk_products_on_created_by_user
        FOREIGN KEY (created_by_user_id)
            REFERENCES users (id);
