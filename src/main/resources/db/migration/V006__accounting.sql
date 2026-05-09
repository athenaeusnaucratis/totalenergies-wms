CREATE TABLE gl_account (
    id              BIGSERIAL PRIMARY KEY,
    account_code    VARCHAR(20) NOT NULL UNIQUE,
    name            VARCHAR(200) NOT NULL,
    account_type    VARCHAR(20) NOT NULL,
    description     TEXT,
    parent_id       BIGINT REFERENCES gl_account(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE acctg_trans (
    id              BIGSERIAL PRIMARY KEY,
    trans_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    description     TEXT,
    source_type     VARCHAR(30),
    source_id       BIGINT,
    is_posted       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE acctg_trans_entry (
    id              BIGSERIAL PRIMARY KEY,
    acctg_trans_id  BIGINT NOT NULL REFERENCES acctg_trans(id),
    gl_account_id   BIGINT NOT NULL REFERENCES gl_account(id),
    debit_amount    NUMERIC(14,2) NOT NULL DEFAULT 0,
    credit_amount   NUMERIC(14,2) NOT NULL DEFAULT 0,
    product_id      BIGINT REFERENCES product(id),
    description     TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE invoice (
    id              BIGSERIAL PRIMARY KEY,
    invoice_type    VARCHAR(20) NOT NULL,
    company_id      BIGINT NOT NULL REFERENCES company(id),
    order_id        BIGINT,
    invoice_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date        DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    total_amount    NUMERIC(14,2) NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE invoice_item (
    id              BIGSERIAL PRIMARY KEY,
    invoice_id      BIGINT NOT NULL REFERENCES invoice(id),
    product_id      BIGINT REFERENCES product(id),
    quantity        NUMERIC(12,2) NOT NULL,
    unit_price      NUMERIC(12,2) NOT NULL,
    amount          NUMERIC(14,2) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE product_costing (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT NOT NULL UNIQUE REFERENCES product(id),
    avg_unit_cost   NUMERIC(14,4) NOT NULL DEFAULT 0,
    total_qty       NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_value     NUMERIC(14,2) NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Seed chart of accounts
INSERT INTO gl_account (account_code, name, account_type, description) VALUES
('1000', 'Inventory', 'ASSET', 'Warehouse inventory asset'),
('1100', 'Accounts Receivable', 'ASSET', 'Customer receivables'),
('2000', 'Accounts Payable', 'LIABILITY', 'Supplier payables'),
('4000', 'Revenue', 'REVENUE', 'Sales revenue'),
('5000', 'Cost of Goods Sold', 'EXPENSE', 'COGS');

CREATE INDEX idx_acctg_trans_entry_trans ON acctg_trans_entry(acctg_trans_id);
CREATE INDEX idx_acctg_trans_source ON acctg_trans(source_type, source_id);
CREATE INDEX idx_invoice_company ON invoice(company_id);
CREATE INDEX idx_invoice_item_invoice ON invoice_item(invoice_id);
CREATE INDEX idx_product_costing_product ON product_costing(product_id);
