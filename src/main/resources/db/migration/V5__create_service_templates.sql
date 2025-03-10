CREATE TABLE service_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    service_type VARCHAR(100) NOT NULL,
    kilometrage_interval INTEGER,
    month_interval INTEGER,
    check_list TEXT,
    required_parts TEXT,
    estimated_duration INTEGER,
    estimated_cost DECIMAL(10,2),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
); 