ALTER TABLE order_intents
    ADD COLUMN total_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00;

ALTER TABLE order_intents
    ALTER COLUMN total_amount DROP DEFAULT;
