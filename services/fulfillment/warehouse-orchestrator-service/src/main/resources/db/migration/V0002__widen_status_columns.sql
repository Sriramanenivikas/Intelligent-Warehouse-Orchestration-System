-- Widen status columns to accommodate new lifecycle states (IN_PROGRESS, COMPLETED, etc.)
ALTER TABLE warehouse_orchestration.fulfillment_tasks
    ALTER COLUMN status TYPE VARCHAR(32);

ALTER TABLE warehouse_orchestration.fulfillment_orders
    ALTER COLUMN status TYPE VARCHAR(32);
