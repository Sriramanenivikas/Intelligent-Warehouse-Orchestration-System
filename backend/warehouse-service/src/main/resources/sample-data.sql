-- Sample Data for Warehouse Service Testing
-- This file provides sample warehouse data to test the geospatial allocation algorithm

-- Note: Run this after the application creates the schema
-- Or use the REST API to create warehouses via POST /api/v1/warehouses

-- Clear existing data (optional)
-- TRUNCATE TABLE warehouses CASCADE;

-- Sample Warehouses in Different US Cities
-- These warehouses are strategically placed to test the allocation algorithm

-- 1. New York Central Warehouse (High Priority)
INSERT INTO warehouses (
    code, name, address, city, state, country, postal_code,
    latitude, longitude, capacity_sqm, max_capacity, current_load, priority,
    is_active, inventory, manager_name, contact_phone, contact_email, operating_hours,
    created_at, updated_at
) VALUES (
    'WH-NYC-001',
    'New York Central Warehouse',
    '123 Broadway Ave',
    'New York',
    'NY',
    'USA',
    '10001',
    40.7128,
    -74.0060,
    5000.00,
    10000,
    2000,
    8,
    true,
    '{"SKU-001": 1000, "SKU-002": 1500, "SKU-003": 800, "SKU-004": 500, "SKU-005": 2000}',
    'John Smith',
    '+1-212-555-0100',
    'john.smith@iwos.com',
    'Mon-Fri 6AM-10PM',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 2. Brooklyn Warehouse (Medium Priority, Higher Load)
INSERT INTO warehouses (
    code, name, address, city, state, country, postal_code,
    latitude, longitude, capacity_sqm, max_capacity, current_load, priority,
    is_active, inventory, manager_name, contact_phone, contact_email, operating_hours,
    created_at, updated_at
) VALUES (
    'WH-BKN-001',
    'Brooklyn Distribution Center',
    '456 Atlantic Ave',
    'Brooklyn',
    'NY',
    'USA',
    '11201',
    40.6782,
    -73.9442,
    3500.00,
    8000,
    5500,
    6,
    true,
    '{"SKU-001": 500, "SKU-002": 600, "SKU-003": 400, "SKU-006": 1000}',
    'Sarah Johnson',
    '+1-718-555-0200',
    'sarah.johnson@iwos.com',
    'Mon-Sat 7AM-9PM',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 3. Newark Warehouse (Close to NYC, Medium Priority)
INSERT INTO warehouses (
    code, name, address, city, state, country, postal_code,
    latitude, longitude, capacity_sqm, max_capacity, current_load, priority,
    is_active, inventory, manager_name, contact_phone, contact_email, operating_hours,
    created_at, updated_at
) VALUES (
    'WH-EWR-001',
    'Newark Logistics Hub',
    '789 Market Street',
    'Newark',
    'NJ',
    'USA',
    '07102',
    40.7357,
    -74.1724,
    6000.00,
    12000,
    3000,
    7,
    true,
    '{"SKU-001": 2000, "SKU-002": 2500, "SKU-003": 1000, "SKU-004": 800, "SKU-005": 1500, "SKU-007": 500}',
    'Michael Chen',
    '+1-973-555-0300',
    'michael.chen@iwos.com',
    '24/7',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 4. Los Angeles Warehouse (West Coast)
INSERT INTO warehouses (
    code, name, address, city, state, country, postal_code,
    latitude, longitude, capacity_sqm, max_capacity, current_load, priority,
    is_active, inventory, manager_name, contact_phone, contact_email, operating_hours,
    created_at, updated_at
) VALUES (
    'WH-LAX-001',
    'Los Angeles Distribution Center',
    '1000 Commerce Way',
    'Los Angeles',
    'CA',
    'USA',
    '90001',
    34.0522,
    -118.2437,
    8000.00,
    15000,
    4500,
    9,
    true,
    '{"SKU-001": 3000, "SKU-002": 3500, "SKU-003": 2000, "SKU-004": 1500, "SKU-005": 2500, "SKU-008": 1000}',
    'Emily Rodriguez',
    '+1-213-555-0400',
    'emily.rodriguez@iwos.com',
    '24/7',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 5. Chicago Warehouse (Midwest Hub)
INSERT INTO warehouses (
    code, name, address, city, state, country, postal_code,
    latitude, longitude, capacity_sqm, max_capacity, current_load, priority,
    is_active, inventory, manager_name, contact_phone, contact_email, operating_hours,
    created_at, updated_at
) VALUES (
    'WH-CHI-001',
    'Chicago Midwest Hub',
    '500 Lake Shore Drive',
    'Chicago',
    'IL',
    'USA',
    '60601',
    41.8781,
    -87.6298,
    7000.00,
    14000,
    5000,
    7,
    true,
    '{"SKU-001": 1800, "SKU-002": 2000, "SKU-003": 1200, "SKU-004": 900, "SKU-009": 800}',
    'David Kim',
    '+1-312-555-0500',
    'david.kim@iwos.com',
    'Mon-Fri 6AM-10PM',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 6. Queens Warehouse (Near NYC, Low Load)
INSERT INTO warehouses (
    code, name, address, city, state, country, postal_code,
    latitude, longitude, capacity_sqm, max_capacity, current_load, priority,
    is_active, inventory, manager_name, contact_phone, contact_email, operating_hours,
    created_at, updated_at
) VALUES (
    'WH-QNS-001',
    'Queens Fulfillment Center',
    '200 Northern Blvd',
    'Queens',
    'NY',
    'USA',
    '11101',
    40.7282,
    -73.7949,
    4000.00,
    9000,
    1500,
    5,
    true,
    '{"SKU-001": 800, "SKU-002": 900, "SKU-003": 600, "SKU-005": 1200}',
    'Lisa Wang',
    '+1-718-555-0600',
    'lisa.wang@iwos.com',
    'Mon-Sat 8AM-8PM',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 7. Philadelphia Warehouse
INSERT INTO warehouses (
    code, name, address, city, state, country, postal_code,
    latitude, longitude, capacity_sqm, max_capacity, current_load, priority,
    is_active, inventory, manager_name, contact_phone, contact_email, operating_hours,
    created_at, updated_at
) VALUES (
    'WH-PHL-001',
    'Philadelphia Distribution Center',
    '300 Market Street',
    'Philadelphia',
    'PA',
    'USA',
    '19101',
    39.9526,
    -75.1652,
    5500.00,
    11000,
    3500,
    6,
    true,
    '{"SKU-001": 1200, "SKU-002": 1400, "SKU-003": 800, "SKU-004": 600, "SKU-010": 500}',
    'Robert Martinez',
    '+1-215-555-0700',
    'robert.martinez@iwos.com',
    'Mon-Fri 7AM-9PM',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 8. Inactive Warehouse (For Testing Filtering)
INSERT INTO warehouses (
    code, name, address, city, state, country, postal_code,
    latitude, longitude, capacity_sqm, max_capacity, current_load, priority,
    is_active, inventory, manager_name, contact_phone, contact_email, operating_hours,
    created_at, updated_at
) VALUES (
    'WH-NYC-002',
    'Manhattan Old Warehouse (Inactive)',
    '999 Old St',
    'New York',
    'NY',
    'USA',
    '10002',
    40.7200,
    -73.9900,
    2000.00,
    5000,
    4500,
    3,
    false, -- INACTIVE
    '{"SKU-001": 100, "SKU-002": 200}',
    'Inactive Manager',
    '+1-212-555-9999',
    'inactive@iwos.com',
    'Closed',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Test Scenarios:

-- Scenario 1: Customer in Manhattan (40.7580, -73.9855)
-- Expected: WH-NYC-001 (closest, high priority, has inventory)
-- Distance: ~5-6 km

-- Scenario 2: Customer in Brooklyn (40.6500, -73.9500)
-- Expected: WH-BKN-001 (closest) OR WH-NYC-001 (higher priority, less load)
-- Distance: ~3-4 km to BKN, ~8 km to NYC

-- Scenario 3: Customer in Newark (40.7357, -74.1724)
-- Expected: WH-EWR-001 (same location!)
-- Distance: 0 km

-- Scenario 4: Customer in Long Island (far from all)
-- Expected: NULL or WH-QNS-001 (if within 50km for STANDARD delivery)

-- Scenario 5: EXPRESS delivery from Manhattan
-- Expected: Only warehouses within 10km considered

-- Scenario 6: STANDARD delivery from Manhattan
-- Expected: Warehouses within 50km considered

-- Statistics Queries for Validation:

-- Count active warehouses
SELECT COUNT(*) as active_warehouses FROM warehouses WHERE is_active = true;

-- Check inventory distribution
SELECT code, name, city, jsonb_object_keys(inventory) as sku,
       (inventory->jsonb_object_keys(inventory))::int as quantity
FROM warehouses
WHERE is_active = true
ORDER BY code, sku;

-- Check warehouse load percentages
SELECT code, name, city,
       current_load, max_capacity,
       ROUND((current_load::numeric / max_capacity::numeric * 100), 2) as load_percentage
FROM warehouses
WHERE is_active = true
ORDER BY load_percentage;

-- Find warehouses with specific SKU
SELECT code, name, city,
       (inventory->>'SKU-001')::int as sku001_quantity
FROM warehouses
WHERE is_active = true
AND inventory ? 'SKU-001'
AND (inventory->>'SKU-001')::int > 0
ORDER BY (inventory->>'SKU-001')::int DESC;

-- Calculate distances from Manhattan (40.7580, -73.9855)
SELECT
    code,
    name,
    city,
    latitude,
    longitude,
    ROUND(
        (6371 * acos(
            cos(radians(40.7580)) *
            cos(radians(latitude)) *
            cos(radians(longitude) - radians(-73.9855)) +
            sin(radians(40.7580)) *
            sin(radians(latitude))
        ))::numeric,
        2
    ) as distance_km
FROM warehouses
WHERE is_active = true
ORDER BY distance_km;
