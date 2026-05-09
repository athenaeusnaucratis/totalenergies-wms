CREATE TABLE purchase_order (
    id              BIGSERIAL PRIMARY KEY,
    supplier_id     BIGINT NOT NULL REFERENCES company(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    order_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    expected_date   DATE,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE purchase_order_line (
    id                  BIGSERIAL PRIMARY KEY,
    purchase_order_id   BIGINT NOT NULL REFERENCES purchase_order(id),
    product_id          BIGINT NOT NULL REFERENCES product(id),
    quantity            NUMERIC(12,2) NOT NULL,
    unit_price          NUMERIC(12,2) NOT NULL,
    received_qty        NUMERIC(12,2) NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE goods_receipt (
    id                  BIGSERIAL PRIMARY KEY,
    purchase_order_id   BIGINT NOT NULL REFERENCES purchase_order(id),
    receipt_date        DATE NOT NULL DEFAULT CURRENT_DATE,
    notes               TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE goods_receipt_line (
    id                  BIGSERIAL PRIMARY KEY,
    goods_receipt_id    BIGINT NOT NULL REFERENCES goods_receipt(id),
    po_line_id          BIGINT NOT NULL REFERENCES purchase_order_line(id),
    stock_item_id       BIGINT REFERENCES stock_item(id),
    quantity_received   NUMERIC(12,2) NOT NULL,
    location_id         BIGINT REFERENCES stock_location(id),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_po_supplier ON purchase_order(supplier_id);
CREATE INDEX idx_po_status ON purchase_order(status);
CREATE INDEX idx_po_line_order ON purchase_order_line(purchase_order_id);
