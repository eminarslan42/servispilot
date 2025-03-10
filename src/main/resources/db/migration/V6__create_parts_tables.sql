-- Parça tablosu
CREATE TABLE parts (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(100),
    model VARCHAR(100),
    category VARCHAR(100),
    description TEXT,
    quantity INTEGER DEFAULT 0,
    minimum_quantity INTEGER DEFAULT 0,
    purchase_price DECIMAL(10,2),
    selling_price DECIMAL(10,2),
    location VARCHAR(100),
    unit VARCHAR(50),
    supplier VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_stock_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Parça hareketi tablosu
CREATE TABLE part_movements (
    id BIGSERIAL PRIMARY KEY,
    part_id BIGINT NOT NULL REFERENCES parts(id),
    movement_type VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL,
    reason VARCHAR(255),
    unit_price DECIMAL(10,2),
    document_no VARCHAR(100),
    notes TEXT,
    service_record_id BIGINT REFERENCES service_records(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_movement_part FOREIGN KEY (part_id) REFERENCES parts(id) ON DELETE CASCADE,
    CONSTRAINT fk_movement_service FOREIGN KEY (service_record_id) REFERENCES service_records(id) ON DELETE SET NULL
); 