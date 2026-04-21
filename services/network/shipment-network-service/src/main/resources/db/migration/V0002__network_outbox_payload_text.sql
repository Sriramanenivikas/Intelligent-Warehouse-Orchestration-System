ALTER TABLE shipment_network.network_outbox_events
    ALTER COLUMN payload TYPE TEXT USING payload::TEXT;
