CREATE TABLE service_reminders (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL REFERENCES vehicles(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    reminder_date TIMESTAMP NOT NULL,
    next_service_date TIMESTAMP,
    next_service_kilometer INTEGER,
    status VARCHAR(50) NOT NULL,
    reminder_type VARCHAR(100) NOT NULL,
    notification_sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_reminder_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
); 