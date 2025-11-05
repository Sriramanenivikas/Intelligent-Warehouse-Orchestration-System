-- ========================================
-- IWOS Database Initialization Script
-- ========================================

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";  -- For full-text search

-- ========================================
-- USERS & AUTHENTICATION SCHEMA
-- ========================================

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    is_email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    CONSTRAINT email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$')
);

-- Roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Permissions table
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    resource VARCHAR(50) NOT NULL,  -- inventory, orders, warehouse, users
    action VARCHAR(50) NOT NULL,    -- read, write, delete, admin
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User-Role mapping (many-to-many)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

-- Role-Permission mapping (many-to-many)
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- Refresh tokens for JWT
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN DEFAULT FALSE,
    revoked_at TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent TEXT
);

-- ========================================
-- WAREHOUSE SCHEMA
-- ========================================

-- Warehouses table
CREATE TABLE warehouses (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    capacity_sqm DECIMAL(10, 2),
    is_active BOOLEAN DEFAULT TRUE,
    manager_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Storage zones within warehouses
CREATE TABLE zones (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    zone_type VARCHAR(50) NOT NULL,  -- RECEIVING, STORAGE, PICKING, PACKING, SHIPPING
    capacity INTEGER NOT NULL,
    current_utilization INTEGER DEFAULT 0,
    temperature_controlled BOOLEAN DEFAULT FALSE,
    temperature_min DECIMAL(5, 2),
    temperature_max DECIMAL(5, 2),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (warehouse_id, code),
    CONSTRAINT utilization_check CHECK (current_utilization >= 0 AND current_utilization <= capacity)
);

-- ========================================
-- INVENTORY SCHEMA
-- ========================================

-- SKU (Stock Keeping Unit) master data
CREATE TABLE skus (
    id BIGSERIAL PRIMARY KEY,
    sku_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    subcategory VARCHAR(100),
    brand VARCHAR(100),
    unit_price DECIMAL(10, 2) NOT NULL,
    cost_price DECIMAL(10, 2),
    weight_kg DECIMAL(8, 3),
    dimensions_cm VARCHAR(50),  -- Format: LxWxH
    barcode VARCHAR(100),
    qr_code VARCHAR(255),
    reorder_point INTEGER DEFAULT 10,
    reorder_quantity INTEGER DEFAULT 100,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT price_check CHECK (unit_price >= 0 AND cost_price >= 0)
);

-- Inventory levels per warehouse
CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    sku_id BIGINT NOT NULL REFERENCES skus(id) ON DELETE CASCADE,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    zone_id BIGINT REFERENCES zones(id) ON DELETE SET NULL,
    quantity_on_hand INTEGER DEFAULT 0,
    quantity_reserved INTEGER DEFAULT 0,
    quantity_available INTEGER GENERATED ALWAYS AS (quantity_on_hand - quantity_reserved) STORED,
    last_counted_at TIMESTAMP,
    last_restocked_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (sku_id, warehouse_id),
    CONSTRAINT quantity_check CHECK (
        quantity_on_hand >= 0 AND
        quantity_reserved >= 0 AND
        quantity_reserved <= quantity_on_hand
    )
);

-- Inventory transactions (audit trail)
CREATE TABLE inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    inventory_id BIGINT NOT NULL REFERENCES inventory(id) ON DELETE CASCADE,
    transaction_type VARCHAR(50) NOT NULL,  -- RESTOCK, ADJUSTMENT, RESERVATION, RELEASE, PICK, DAMAGE, RETURN
    quantity INTEGER NOT NULL,
    quantity_before INTEGER NOT NULL,
    quantity_after INTEGER NOT NULL,
    reference_type VARCHAR(50),  -- ORDER, PURCHASE_ORDER, ADJUSTMENT
    reference_id BIGINT,
    notes TEXT,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- ORDERS SCHEMA
-- ========================================

-- Order status enum type
CREATE TYPE order_status AS ENUM (
    'PENDING',
    'CONFIRMED',
    'PROCESSING',
    'PICKING',
    'PACKING',
    'READY_TO_SHIP',
    'SHIPPED',
    'DELIVERED',
    'CANCELLED',
    'FAILED'
);

-- Orders table
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255),
    customer_phone VARCHAR(20),
    shipping_address TEXT NOT NULL,
    shipping_city VARCHAR(100),
    shipping_state VARCHAR(100),
    shipping_country VARCHAR(100),
    shipping_postal_code VARCHAR(20),
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    status order_status DEFAULT 'PENDING',
    priority INTEGER DEFAULT 0,  -- Higher number = higher priority
    total_items INTEGER DEFAULT 0,
    total_amount DECIMAL(12, 2) DEFAULT 0.00,
    notes TEXT,
    assigned_to BIGINT REFERENCES users(id),  -- Worker assigned
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    CONSTRAINT total_check CHECK (total_items >= 0 AND total_amount >= 0)
);

-- Order items (line items)
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    sku_id BIGINT NOT NULL REFERENCES skus(id),
    quantity_ordered INTEGER NOT NULL,
    quantity_picked INTEGER DEFAULT 0,
    unit_price DECIMAL(10, 2) NOT NULL,
    line_total DECIMAL(12, 2) GENERATED ALWAYS AS (quantity_ordered * unit_price) STORED,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT quantity_check CHECK (
        quantity_ordered > 0 AND
        quantity_picked >= 0 AND
        quantity_picked <= quantity_ordered
    )
);

-- Order status history (audit trail)
CREATE TABLE order_status_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    status_from order_status,
    status_to order_status NOT NULL,
    changed_by BIGINT REFERENCES users(id),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- INDEXES FOR PERFORMANCE
-- ========================================

-- Users indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_is_active ON users(is_active);

-- Refresh tokens indexes
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens(revoked);

-- Warehouses indexes
CREATE INDEX idx_warehouses_code ON warehouses(code);
CREATE INDEX idx_warehouses_is_active ON warehouses(is_active);

-- Zones indexes
CREATE INDEX idx_zones_warehouse_id ON zones(warehouse_id);
CREATE INDEX idx_zones_zone_type ON zones(zone_type);

-- SKUs indexes
CREATE INDEX idx_skus_sku_code ON skus(sku_code);
CREATE INDEX idx_skus_category ON skus(category);
CREATE INDEX idx_skus_is_active ON skus(is_active);
CREATE INDEX idx_skus_name_trgm ON skus USING gin(name gin_trgm_ops);  -- Full-text search

-- Inventory indexes
CREATE INDEX idx_inventory_sku_id ON inventory(sku_id);
CREATE INDEX idx_inventory_warehouse_id ON inventory(warehouse_id);
CREATE INDEX idx_inventory_zone_id ON inventory(zone_id);
CREATE INDEX idx_inventory_quantity_available ON inventory(quantity_available);

-- Inventory transactions indexes
CREATE INDEX idx_inventory_transactions_inventory_id ON inventory_transactions(inventory_id);
CREATE INDEX idx_inventory_transactions_created_at ON inventory_transactions(created_at);
CREATE INDEX idx_inventory_transactions_reference ON inventory_transactions(reference_type, reference_id);

-- Orders indexes
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_orders_warehouse_id ON orders(warehouse_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);
CREATE INDEX idx_orders_assigned_to ON orders(assigned_to);
CREATE INDEX idx_orders_customer_email ON orders(customer_email);

-- Order items indexes
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_sku_id ON order_items(sku_id);

-- Order status history indexes
CREATE INDEX idx_order_status_history_order_id ON order_status_history(order_id);
CREATE INDEX idx_order_status_history_created_at ON order_status_history(created_at DESC);

-- ========================================
-- INSERT DEFAULT DATA
-- ========================================

-- Insert default roles
INSERT INTO roles (name, description) VALUES
('ADMIN', 'Full system access'),
('MANAGER', 'Warehouse manager with elevated privileges'),
('OPERATOR', 'Warehouse operator for order processing'),
('VIEWER', 'Read-only access for reporting');

-- Insert default permissions
INSERT INTO permissions (name, resource, action, description) VALUES
-- Inventory permissions
('inventory:read', 'inventory', 'read', 'View inventory data'),
('inventory:write', 'inventory', 'write', 'Modify inventory data'),
('inventory:delete', 'inventory', 'delete', 'Delete inventory records'),
('inventory:admin', 'inventory', 'admin', 'Full inventory administration'),

-- Order permissions
('orders:read', 'orders', 'read', 'View orders'),
('orders:write', 'orders', 'write', 'Create and modify orders'),
('orders:delete', 'orders', 'delete', 'Cancel orders'),
('orders:admin', 'orders', 'admin', 'Full order administration'),

-- Warehouse permissions
('warehouse:read', 'warehouse', 'read', 'View warehouse data'),
('warehouse:write', 'warehouse', 'write', 'Modify warehouse configuration'),
('warehouse:delete', 'warehouse', 'delete', 'Delete warehouses'),
('warehouse:admin', 'warehouse', 'admin', 'Full warehouse administration'),

-- User permissions
('users:read', 'users', 'read', 'View user data'),
('users:write', 'users', 'write', 'Create and modify users'),
('users:delete', 'users', 'delete', 'Delete users'),
('users:admin', 'users', 'admin', 'Full user administration');

-- Assign permissions to roles
-- ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    id
FROM permissions;

-- MANAGER gets read/write for inventory, orders, warehouse
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT id FROM roles WHERE name = 'MANAGER'),
    id
FROM permissions
WHERE action IN ('read', 'write');

-- OPERATOR gets read/write for orders and inventory (read only)
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT id FROM roles WHERE name = 'OPERATOR'),
    id
FROM permissions
WHERE (resource = 'orders' AND action IN ('read', 'write'))
   OR (resource = 'inventory' AND action = 'read');

-- VIEWER gets read-only access
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT id FROM roles WHERE name = 'VIEWER'),
    id
FROM permissions
WHERE action = 'read';

-- Create default admin user (password: Admin@123)
-- BCrypt hash for 'Admin@123'
INSERT INTO users (username, email, password_hash, first_name, last_name, is_active, is_email_verified)
VALUES (
    'admin',
    'admin@iwos.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5/YlKKBpCQI5.',
    'System',
    'Administrator',
    TRUE,
    TRUE
);

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id)
VALUES (
    (SELECT id FROM users WHERE username = 'admin'),
    (SELECT id FROM roles WHERE name = 'ADMIN')
);

-- Insert sample warehouse
INSERT INTO warehouses (code, name, address, city, state, country, postal_code, capacity_sqm, is_active)
VALUES (
    'WH001',
    'Main Warehouse - North',
    '123 Industrial Pkwy',
    'Chicago',
    'Illinois',
    'USA',
    '60601',
    10000.00,
    TRUE
);

-- Insert sample zones
INSERT INTO zones (warehouse_id, code, name, zone_type, capacity, is_active)
VALUES
    ((SELECT id FROM warehouses WHERE code = 'WH001'), 'RCV-01', 'Receiving Zone 1', 'RECEIVING', 500, TRUE),
    ((SELECT id FROM warehouses WHERE code = 'WH001'), 'STR-A1', 'Storage Zone A1', 'STORAGE', 2000, TRUE),
    ((SELECT id FROM warehouses WHERE code = 'WH001'), 'STR-A2', 'Storage Zone A2', 'STORAGE', 2000, TRUE),
    ((SELECT id FROM warehouses WHERE code = 'WH001'), 'PICK-01', 'Picking Zone 1', 'PICKING', 1000, TRUE),
    ((SELECT id FROM warehouses WHERE code = 'WH001'), 'PACK-01', 'Packing Zone 1', 'PACKING', 500, TRUE),
    ((SELECT id FROM warehouses WHERE code = 'WH001'), 'SHIP-01', 'Shipping Zone 1', 'SHIPPING', 500, TRUE);

-- Insert sample SKUs
INSERT INTO skus (sku_code, name, description, category, brand, unit_price, cost_price, weight_kg, reorder_point, reorder_quantity)
VALUES
    ('SKU-001', 'Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 'Electronics', 'TechBrand', 29.99, 15.00, 0.15, 50, 200),
    ('SKU-002', 'Laptop Stand', 'Adjustable aluminum laptop stand', 'Accessories', 'OfficePro', 49.99, 25.00, 1.20, 30, 100),
    ('SKU-003', 'USB-C Cable', 'USB-C to USB-A cable, 6ft', 'Electronics', 'TechBrand', 12.99, 6.00, 0.05, 100, 500),
    ('SKU-004', 'Desk Organizer', 'Bamboo desk organizer with compartments', 'Office Supplies', 'EcoOffice', 34.99, 18.00, 0.80, 20, 50),
    ('SKU-005', 'Mechanical Keyboard', 'RGB mechanical gaming keyboard', 'Electronics', 'GameTech', 129.99, 65.00, 1.50, 25, 100);

-- Insert sample inventory
INSERT INTO inventory (sku_id, warehouse_id, zone_id, quantity_on_hand)
VALUES
    ((SELECT id FROM skus WHERE sku_code = 'SKU-001'), (SELECT id FROM warehouses WHERE code = 'WH001'), (SELECT id FROM zones WHERE code = 'STR-A1'), 250),
    ((SELECT id FROM skus WHERE sku_code = 'SKU-002'), (SELECT id FROM warehouses WHERE code = 'WH001'), (SELECT id FROM zones WHERE code = 'STR-A1'), 150),
    ((SELECT id FROM skus WHERE sku_code = 'SKU-003'), (SELECT id FROM warehouses WHERE code = 'WH001'), (SELECT id FROM zones WHERE code = 'STR-A2'), 800),
    ((SELECT id FROM skus WHERE sku_code = 'SKU-004'), (SELECT id FROM warehouses WHERE code = 'WH001'), (SELECT id FROM zones WHERE code = 'STR-A2'), 100),
    ((SELECT id FROM skus WHERE sku_code = 'SKU-005'), (SELECT id FROM warehouses WHERE code = 'WH001'), (SELECT id FROM zones WHERE code = 'STR-A1'), 75);

-- ========================================
-- TRIGGERS
-- ========================================

-- Update timestamp trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply update trigger to tables
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_warehouses_updated_at BEFORE UPDATE ON warehouses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_zones_updated_at BEFORE UPDATE ON zones
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_skus_updated_at BEFORE UPDATE ON skus
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_inventory_updated_at BEFORE UPDATE ON inventory
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Trigger to update order total_items when order_items change
CREATE OR REPLACE FUNCTION update_order_totals()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE orders
    SET
        total_items = (SELECT COALESCE(SUM(quantity_ordered), 0) FROM order_items WHERE order_id = NEW.order_id),
        total_amount = (SELECT COALESCE(SUM(line_total), 0) FROM order_items WHERE order_id = NEW.order_id)
    WHERE id = NEW.order_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_order_totals_trigger
AFTER INSERT OR UPDATE ON order_items
FOR EACH ROW EXECUTE FUNCTION update_order_totals();

-- Trigger to log order status changes
CREATE OR REPLACE FUNCTION log_order_status_change()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'UPDATE' AND OLD.status <> NEW.status) THEN
        INSERT INTO order_status_history (order_id, status_from, status_to, changed_by)
        VALUES (NEW.id, OLD.status, NEW.status, NEW.updated_at::text::bigint);  -- Simplified, should get from context
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER log_order_status_trigger
AFTER UPDATE ON orders
FOR EACH ROW EXECUTE FUNCTION log_order_status_change();

-- ========================================
-- VIEWS FOR COMMON QUERIES
-- ========================================

-- View: Inventory with SKU details
CREATE OR REPLACE VIEW v_inventory_details AS
SELECT
    i.id AS inventory_id,
    s.sku_code,
    s.name AS sku_name,
    s.category,
    w.code AS warehouse_code,
    w.name AS warehouse_name,
    z.code AS zone_code,
    z.name AS zone_name,
    i.quantity_on_hand,
    i.quantity_reserved,
    i.quantity_available,
    s.reorder_point,
    s.reorder_quantity,
    CASE
        WHEN i.quantity_available <= s.reorder_point THEN 'LOW'
        WHEN i.quantity_available = 0 THEN 'OUT_OF_STOCK'
        ELSE 'OK'
    END AS stock_status,
    i.last_counted_at,
    i.last_restocked_at
FROM inventory i
JOIN skus s ON i.sku_id = s.id
JOIN warehouses w ON i.warehouse_id = w.id
LEFT JOIN zones z ON i.zone_id = z.id;

-- View: Orders with details
CREATE OR REPLACE VIEW v_order_details AS
SELECT
    o.id AS order_id,
    o.order_number,
    o.customer_name,
    o.customer_email,
    o.status,
    o.priority,
    o.total_items,
    o.total_amount,
    w.code AS warehouse_code,
    w.name AS warehouse_name,
    u.username AS assigned_to_username,
    o.created_at,
    o.updated_at,
    o.confirmed_at,
    o.shipped_at,
    o.delivered_at
FROM orders o
JOIN warehouses w ON o.warehouse_id = w.id
LEFT JOIN users u ON o.assigned_to = u.id;

-- ========================================
-- GRANTS (Optional - for specific user access)
-- ========================================

-- Grant access to iwos_user
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO iwos_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO iwos_user;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO iwos_user;

-- ========================================
-- COMPLETION MESSAGE
-- ========================================

DO $$
BEGIN
    RAISE NOTICE '✅ IWOS Database initialized successfully!';
    RAISE NOTICE '📊 Tables created: %', (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE');
    RAISE NOTICE '👤 Default admin user created: admin@iwos.com (password: Admin@123)';
    RAISE NOTICE '🏭 Sample warehouse: WH001';
    RAISE NOTICE '📦 Sample SKUs: 5';
END $$;
