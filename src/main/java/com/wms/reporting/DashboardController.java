package com.wms.reporting;

import com.wms.product.CompanyRepository;
import com.wms.product.ProductRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ProductRepository productRepo;
    private final CompanyRepository companyRepo;
    private final JdbcTemplate jdbc;

    public DashboardController(ProductRepository productRepo, CompanyRepository companyRepo, JdbcTemplate jdbc) {
        this.productRepo = productRepo;
        this.companyRepo = companyRepo;
        this.jdbc = jdbc;
    }

    @GetMapping
    public Map<String, Object> getDashboard() {
        long totalProducts = productRepo.count();
        long totalCompanies = companyRepo.count();

        Long openPOs = jdbc.queryForObject(
                "SELECT COUNT(*) FROM purchase_order WHERE status IN ('PENDING','PLACED')", Long.class);
        Long openSOs = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sales_order WHERE status IN ('PENDING','CONFIRMED','ALLOCATED')", Long.class);

        BigDecimal stockValue = jdbc.queryForObject(
                "SELECT COALESCE(SUM(si.quantity * p.cost_price), 0) FROM stock_item si JOIN product p ON si.product_id = p.id WHERE si.status = 'IN_STOCK'",
                BigDecimal.class);

        BigDecimal stockSellingValue = jdbc.queryForObject(
                "SELECT COALESCE(SUM(si.quantity * p.selling_price), 0) FROM stock_item si JOIN product p ON si.product_id = p.id WHERE si.status = 'IN_STOCK'",
                BigDecimal.class);

        Long totalStockItems = jdbc.queryForObject(
                "SELECT COALESCE(SUM(quantity), 0) FROM stock_item WHERE status = 'IN_STOCK'", Long.class);

        return Map.of(
                "totalProducts", totalProducts,
                "totalCompanies", totalCompanies,
                "openPurchaseOrders", openPOs != null ? openPOs : 0,
                "openSalesOrders", openSOs != null ? openSOs : 0,
                "stockCostValue", stockValue != null ? stockValue : BigDecimal.ZERO,
                "stockSellingValue", stockSellingValue != null ? stockSellingValue : BigDecimal.ZERO,
                "totalStockItems", totalStockItems != null ? totalStockItems : 0
        );
    }

    @GetMapping("/stock-by-product")
    public List<Map<String, Object>> getStockByProduct() {
        return jdbc.queryForList(
                "SELECT p.name, p.packaging, p.sku, COALESCE(SUM(si.quantity), 0) as stock, " +
                "p.cost_price, p.selling_price, " +
                "COALESCE(SUM(si.quantity), 0) * p.cost_price as stock_cost_value, " +
                "COALESCE(SUM(si.quantity), 0) * p.selling_price as stock_selling_value " +
                "FROM product p LEFT JOIN stock_item si ON p.id = si.product_id AND si.status = 'IN_STOCK' " +
                "GROUP BY p.id, p.name, p.packaging, p.sku, p.cost_price, p.selling_price " +
                "ORDER BY p.name, p.packaging"
        );
    }

    @GetMapping("/low-stock")
    public List<Map<String, Object>> getLowStock() {
        return jdbc.queryForList(
                "SELECT p.name, p.packaging, p.sku, p.minimum_stock, " +
                "COALESCE(SUM(si.quantity), 0) as current_stock " +
                "FROM product p LEFT JOIN stock_item si ON p.id = si.product_id AND si.status = 'IN_STOCK' " +
                "WHERE p.active = true " +
                "GROUP BY p.id, p.name, p.packaging, p.sku, p.minimum_stock " +
                "HAVING COALESCE(SUM(si.quantity), 0) < p.minimum_stock " +
                "ORDER BY COALESCE(SUM(si.quantity), 0) - p.minimum_stock"
        );
    }
}
