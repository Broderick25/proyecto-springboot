-- =============================================================================
-- ERP LITE - DATABASE SCHEMA
-- Version: 2.0
-- Database: PostgreSQL 17+
-- Encoding: UTF8
-- =============================================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm"; -- For full-text search

-- =============================================================================
-- TABLE: products
-- =============================================================================
CREATE TABLE products (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sku         VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    price       DECIMAL(15, 2) NOT NULL CHECK (price >= 0),
    stock       INT           NOT NULL DEFAULT 0 CHECK (stock >= 0),
    category_id VARCHAR(100),         -- Reference to MongoDB CatalogDocument
    image_url   VARCHAR(500),
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for products
CREATE INDEX idx_products_sku         ON products (sku);
CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_active      ON products (active) WHERE active = TRUE;
CREATE INDEX idx_products_name_trgm   ON products USING gin (name gin_trgm_ops);

-- =============================================================================
-- TABLE: orders
-- Description: Purchase orders (Aggregate Root)
-- =============================================================================
CREATE TABLE orders (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_number  VARCHAR(50)  NOT NULL UNIQUE,
    customer_id   BIGINT       NOT NULL, -- ID from JSONPlaceholder API
    customer_name VARCHAR(200) NOT NULL,
    created_by    VARCHAR(100) NOT NULL, -- Username of authenticated employee
    order_date    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    total_amount  DECIMAL(15, 2) NOT NULL CHECK (total_amount >= 0),
    currency      VARCHAR(3)   NOT NULL DEFAULT 'USD',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_order_status CHECK (
        status IN ('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED')
    )
);

-- Indexes for orders
CREATE INDEX idx_orders_order_number ON orders (order_number);
CREATE INDEX idx_orders_customer_id  ON orders (customer_id);
CREATE INDEX idx_orders_created_by   ON orders (created_by);
CREATE INDEX idx_orders_status       ON orders (status);
CREATE INDEX idx_orders_order_date   ON orders (order_date DESC);

-- =============================================================================
-- TABLE: order_products
-- Description: Order detail lines (Many-to-Many relationship)
-- =============================================================================
CREATE TABLE order_products (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id     UUID        NOT NULL,
    product_id   UUID        NOT NULL,
    product_name VARCHAR(200) NOT NULL, -- Snapshot of product name at order creation
    quantity     INT          NOT NULL CHECK (quantity > 0),
    unit_price   DECIMAL(15, 2) NOT NULL CHECK (unit_price >= 0),
    subtotal     DECIMAL(15, 2) NOT NULL CHECK (subtotal >= 0),

    CONSTRAINT fk_order_products_order FOREIGN KEY (order_id)
        REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_products_product FOREIGN KEY (product_id)
        REFERENCES products (id) ON DELETE RESTRICT
);

-- Indexes for order_products
CREATE INDEX idx_order_products_order_id   ON order_products (order_id);
CREATE INDEX idx_order_products_product_id ON order_products (product_id);