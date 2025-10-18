-- Enable UUID generation if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS public.brands (
  id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
  created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  name VARCHAR(255) NOT NULL,
  updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  CONSTRAINT ukoce3937d2f4mpfqrycbr0l93m UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS public.categories (
  id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
  created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  name VARCHAR(255) NOT NULL,
  updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  parent_category_id UUID,
  FOREIGN KEY (parent_category_id) REFERENCES public.categories (id)
);

CREATE TABLE IF NOT EXISTS public.customers (
  id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
  created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  email VARCHAR(255) NOT NULL,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  phone_number VARCHAR(50),
  updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  CONSTRAINT ukrfbvkrffamfql7cjmen8v976v UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS public.products (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  description TEXT,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  name VARCHAR(255) NOT NULL,
  updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  brand_id UUID,
  category_id UUID,
  FOREIGN KEY (brand_id) REFERENCES public.brands (id),
  FOREIGN KEY (category_id) REFERENCES public.categories (id)
);

CREATE TABLE IF NOT EXISTS public.product_variants (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  attributes TEXT, -- Consider using JSONB for structured attributes
  cost_price NUMERIC(10,2) NOT NULL,
  created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  price NUMERIC(10,2) NOT NULL,
  sku VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  weight NUMERIC(8,2) NOT NULL,
  product_id BIGINT NOT NULL,
  FOREIGN KEY (product_id) REFERENCES public.products (id),
  CONSTRAINT ukq935p2d1pbjm39n0063ghnfgn UNIQUE (sku)
);

CREATE TABLE IF NOT EXISTS public.inventory (
  id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
  last_restocked_at TIMESTAMP(6) WITH TIME ZONE,
  quantity_committed INTEGER NOT NULL DEFAULT 0,
  quantity_on_hand INTEGER NOT NULL DEFAULT 0,
  reorder_level INTEGER NOT NULL DEFAULT 10,
  product_variant_id BIGINT NOT NULL,
  FOREIGN KEY (product_variant_id) REFERENCES public.product_variants (id),
  CONSTRAINT uksmhosiiwyweyooj1okkh3tsqs UNIQUE (product_variant_id)
);

CREATE TABLE IF NOT EXISTS public.inventory_transactions (
  id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
  created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  notes TEXT,
  quantity INTEGER NOT NULL,
  type VARCHAR(255) NOT NULL,
  inventory_id UUID NOT NULL,
  FOREIGN KEY (inventory_id) REFERENCES public.inventory (id),
  CONSTRAINT inventory_transaction_type_check CHECK (type IN ('INBOUND', 'OUTBOUND', 'ADJUSTMENT'))
);

CREATE TABLE IF NOT EXISTS public.orders (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  billing_address TEXT,
  created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  order_date TIMESTAMP(6) WITH TIME ZONE NOT NULL,
  shipping_address TEXT NOT NULL,
  status VARCHAR(255) NOT NULL,
  total_amount NUMERIC(38,2) NOT NULL,
  updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  uuid UUID NOT NULL DEFAULT uuid_generate_v4(),
  customer_id UUID NOT NULL,
  FOREIGN KEY (customer_id) REFERENCES public.customers (id),
  CONSTRAINT uk8trmqe3eqy2ut1xr2i2atcaip UNIQUE (uuid),
  CONSTRAINT order_status_check CHECK (status IN ('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS public.order_items (
  id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
  line_total NUMERIC(38,2) NOT NULL,
  price_per_unit NUMERIC(38,2) NOT NULL,
  quantity INTEGER NOT NULL,
  order_id BIGINT NOT NULL,
  product_variant_id BIGINT NOT NULL,
  FOREIGN KEY (order_id) REFERENCES public.orders (id),
  FOREIGN KEY (product_variant_id) REFERENCES public.product_variants (id)
);

CREATE TABLE IF NOT EXISTS public.users (
  id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(255) NOT NULL,
  username VARCHAR(255) NOT NULL,
  CONSTRAINT ukr43af9ap4edm43mmtq01oddj6 UNIQUE (username),
  CONSTRAINT user_role_check CHECK (role IN ('ADMIN', 'USER'))
);

CREATE TABLE IF NOT EXISTS public.warehouses (
  id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
  address TEXT,
  created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  name VARCHAR(255) NOT NULL,
  updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW()
);