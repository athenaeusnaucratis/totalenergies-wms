CREATE TABLE stock_location (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    parent_id       BIGINT REFERENCES stock_location(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE stock_item (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT NOT NULL REFERENCES product(id),
    location_id     BIGINT REFERENCES stock_location(id),
    quantity         NUMERIC(12,2) NOT NULL DEFAULT 0,
    purchase_price  NUMERIC(12,2),
    batch           VARCHAR(100),
    serial_number   VARCHAR(100),
    expiry_date     DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'IN_STOCK',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE stock_item_tracking (
    id              BIGSERIAL PRIMARY KEY,
    stock_item_id   BIGINT NOT NULL REFERENCES stock_item(id),
    tracking_type   VARCHAR(30) NOT NULL,
    quantity_delta   NUMERIC(12,2) NOT NULL,
    notes           TEXT,
    user_id         BIGINT REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_stock_item_product ON stock_item(product_id);
CREATE INDEX idx_stock_item_location ON stock_item(location_id);
CREATE INDEX idx_stock_item_status ON stock_item(status);
CREATE INDEX idx_stock_tracking_item ON stock_item_tracking(stock_item_id);
