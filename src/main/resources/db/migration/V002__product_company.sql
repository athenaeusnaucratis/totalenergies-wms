CREATE TABLE product_category (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    parent_id       BIGINT REFERENCES product_category(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE product (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    sku             VARCHAR(100) NOT NULL UNIQUE,
    description     TEXT,
    category_id     BIGINT REFERENCES product_category(id),
    unit            VARCHAR(20) NOT NULL DEFAULT 'EACH',
    minimum_stock   INTEGER DEFAULT 0,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE company (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    is_supplier     BOOLEAN NOT NULL DEFAULT FALSE,
    is_customer     BOOLEAN NOT NULL DEFAULT FALSE,
    email           VARCHAR(200),
    phone           VARCHAR(50),
    address         TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE supplier_part (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT NOT NULL REFERENCES product(id),
    supplier_id     BIGINT NOT NULL REFERENCES company(id),
    supplier_sku    VARCHAR(100),
    lead_time_days  INTEGER,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(product_id, supplier_id)
);

CREATE TABLE supplier_price_break (
    id                BIGSERIAL PRIMARY KEY,
    supplier_part_id  BIGINT NOT NULL REFERENCES supplier_part(id),
    quantity          INTEGER NOT NULL,
    price             NUMERIC(12,2) NOT NULL,
    currency          VARCHAR(3) NOT NULL DEFAULT 'USD',
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_product_category ON product(category_id);
CREATE INDEX idx_product_sku ON product(sku);
CREATE INDEX idx_supplier_part_product ON supplier_part(product_id);
CREATE INDEX idx_supplier_part_supplier ON supplier_part(supplier_id);
