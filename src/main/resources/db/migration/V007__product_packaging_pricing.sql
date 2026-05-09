ALTER TABLE product ADD COLUMN packaging VARCHAR(50) NOT NULL DEFAULT '';
ALTER TABLE product ADD COLUMN cost_price NUMERIC(12,2) DEFAULT 0;
ALTER TABLE product ADD COLUMN selling_price NUMERIC(12,2) DEFAULT 0;

ALTER TABLE stock_item ADD COLUMN production_date DATE;

-- Drop the old unique constraint on sku alone (same product can have multiple packaging)
ALTER TABLE product DROP CONSTRAINT IF EXISTS product_sku_key;
ALTER TABLE product ADD CONSTRAINT product_sku_packaging_unique UNIQUE(sku, packaging);

-- Seed product categories
INSERT INTO product_category (name, description) VALUES
('Engine Oils', 'Automotive and industrial engine oils'),
('Transmission Fluids', 'ATF and transmission fluids'),
('Greases', 'Industrial and automotive greases'),
('Coolants', 'Antifreeze and coolants');

-- Seed sample products from client data
INSERT INTO product (name, sku, packaging, selling_price, category_id, unit, minimum_stock, active) VALUES
('Quartz 5000 20w50', 'QTZ-5000-20W50-3X4L', '3x4L', 58, 1, 'BOX', 10, true),
('Quartz 5000 20w50', 'QTZ-5000-20W50-3X5L', '3X5L', 69, 1, 'BOX', 10, true),
('Quartz 5000 20w50', 'QTZ-5000-20W50-208L', '208L', 750, 1, 'EACH', 2, true),
('Quartz 7000 10w40', 'QTZ-7000-10W40-3X4L', '3X4L', 59, 1, 'BOX', 10, true),
('Quartz 7000 10w40', 'QTZ-7000-10W40-3X5L', '3X5L', 74, 1, 'BOX', 10, true),
('Quartz 7000 10w40', 'QTZ-7000-10W40-208L', '208L', 792, 1, 'EACH', 2, true),
('Quartz 9000 5w30', 'QTZ-9000-5W30-12X1L', '12X1L', 66, 1, 'BOX', 10, true),
('Quartz 9000 5w30', 'QTZ-9000-5W30-3X4L', '3X4L', 64, 1, 'BOX', 10, true),
('Quartz 9000 5w30', 'QTZ-9000-5W30-3X5L', '3X5L', 79, 1, 'BOX', 10, true),
('Quartz 9000 5w40', 'QTZ-9000-5W40-12X1L', '12X1L', 66, 1, 'BOX', 10, true),
('Quartz 9000 5w40', 'QTZ-9000-5W40-3X4L', '3X4L', 64, 1, 'BOX', 10, true),
('Quartz 9000 5w40', 'QTZ-9000-5W40-3X5L', '3X5L', 79, 1, 'BOX', 10, true),
('Quartz 9000 0W20', 'QTZ-9000-0W20-3X4L', '3X4L', 74, 1, 'BOX', 10, true),
('Quartz 9000 0W20', 'QTZ-9000-0W20-12X1L', '12X1L', 75, 1, 'BOX', 10, true),
('Rubia TIR 7400 15w40 (Diesel)', 'RUB-7400-15W40-3X5L', '3X5L', 69, 1, 'BOX', 5, true),
('Rubia TIR 7400 15w40 (Diesel)', 'RUB-7400-15W40-208L', '208L (10 000 km)', 735, 1, 'EACH', 2, true),
('Rubia Works 1000 15w40 (Diesel)', 'RUB-W1000-15W40-208L', '208L (20 000 km)', 760, 1, 'EACH', 2, true),
('Fluidmatic DIII (ATF 3)', 'FLUID-DIII-12X1L', '12X1L', 59, 2, 'BOX', 5, true),
('Fluidmatic DIII (ATF 3)', 'FLUID-DIII-3X4L', '3X4L', 61, 2, 'BOX', 5, true),
('Mv Lv (ATF WS)', 'MVLV-ATF-3X4L', '3X4L', 69, 2, 'BOX', 5, true),
('Multis 2 (Solidol)', 'MULTIS2-12X1L', '12X1L', 55, 3, 'BOX', 5, true),
('Multis 3 (Solidol)', 'MULTIS3-12X1L', '12X1L', 58, 3, 'BOX', 5, true),
('Coolelf Eco (Antifreez)', 'COOLELF-ECO-3X4L', '3x4L', 40, 4, 'BOX', 10, true);
