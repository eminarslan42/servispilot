ALTER TABLE vehicles ADD COLUMN created_at TIMESTAMP;
ALTER TABLE service_records ADD COLUMN created_at TIMESTAMP;

UPDATE vehicles SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE service_records SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL; 