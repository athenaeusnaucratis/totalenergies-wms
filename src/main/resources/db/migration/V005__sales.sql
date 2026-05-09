CREATE TABLE sales_order (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL REFERENCES company(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    order_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE sales_order_line (
    id                  BIGSERIAL PRIMARY KEY,
    sales_order_id      BIGINT NOT NULL REFERENCES sales_order(id),
    product_id          BIGINT NOT NULL REFERENCES product(id),
    quantity            NUMERIC(12,2) NOT NULL,
    unit_price          NUMERIC(12,2) NOT NULL,
    allocated_qty       NUMERIC(12,2) NOT NULL DEFAULT 0,
    shipped_qty         NUMERIC(12,2) NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE sales_order_allocation (
    id                  BIGSERIAL PRIMARY KEY,
    so_line_id          BIGINT NOT NULL REFERENCES sales_order_line(id),
    stock_item_id       BIGINT NOT NULL REFERENCES stock_item(id),
    quantity            NUMERIC(12,2) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE shipment (
    id                  BIGSERIAL PRIMARY KEY,
    sales_order_id      BIGINT NOT NULL REFERENCES sales_order(id),
    shipment_date       DATE NOT NULL DEFAULT CURRENT_DATE,
    tracking_number     VARCHAR(200),
    notes               TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE shipment_item (
    id                  BIGSERIAL PRIMARY KEY,
    shipment_id         BIGINT NOT NULL REFERENCES shipment(id),
    so_line_id          BIGINT NOT NULL REFERENCES sales_order_line(id),
    stock_item_id       BIGINT NOT NULL REFERENCES stock_item(id),
    quantity            NUMERIC(12,2) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_so_customer ON sales_order(customer_id);
CREATE INDEX idx_so_status ON sales_order(status);
CREATE INDEX idx_so_line_order ON sales_order_line(sales_order_id);
CREATE INDEX idx_so_alloc_line ON sales_order_allocation(so_line_id);
CREATE INDEX idx_shipment_order ON shipment(sales_order_id);
