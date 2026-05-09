package com.wms.accounting;

import com.wms.common.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AccountingController {

    private final GlAccountRepository glAccountRepo;
    private final AcctgTransRepository transRepo;
    private final InvoiceRepository invoiceRepo;
    private final JdbcTemplate jdbc;

    public AccountingController(GlAccountRepository glAccountRepo, AcctgTransRepository transRepo,
                                InvoiceRepository invoiceRepo, JdbcTemplate jdbc) {
        this.glAccountRepo = glAccountRepo;
        this.transRepo = transRepo;
        this.invoiceRepo = invoiceRepo;
        this.jdbc = jdbc;
    }

    @GetMapping("/gl-accounts")
    public List<GlAccount> listAccounts() { return glAccountRepo.findAll(); }

    @GetMapping("/journal-entries")
    public List<AcctgTrans> listJournals() { return transRepo.findAllByOrderByCreatedAtDesc(); }

    @GetMapping("/invoices")
    public List<Invoice> listInvoices(@RequestParam(required = false) Invoice.InvoiceType type) {
        if (type != null) return invoiceRepo.findByInvoiceType(type);
        return invoiceRepo.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/invoices/{id}")
    public Invoice getInvoice(@PathVariable Long id) {
        return invoiceRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }

    @PostMapping("/invoices/{id}/pay")
    public Invoice payInvoice(@PathVariable Long id) {
        Invoice invoice = invoiceRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        return invoiceRepo.save(invoice);
    }

    @GetMapping("/reports/trial-balance")
    public List<Map<String, Object>> trialBalance() {
        return jdbc.queryForList(
                "SELECT ga.account_code, ga.name, ga.account_type, " +
                "COALESCE(SUM(e.debit_amount), 0) as total_debit, " +
                "COALESCE(SUM(e.credit_amount), 0) as total_credit, " +
                "COALESCE(SUM(e.debit_amount), 0) - COALESCE(SUM(e.credit_amount), 0) as balance " +
                "FROM gl_account ga LEFT JOIN acctg_trans_entry e ON ga.id = e.gl_account_id " +
                "LEFT JOIN acctg_trans t ON e.acctg_trans_id = t.id AND t.is_posted = true " +
                "GROUP BY ga.id, ga.account_code, ga.name, ga.account_type " +
                "ORDER BY ga.account_code"
        );
    }

    @GetMapping("/reports/profit-loss")
    public Map<String, Object> profitLoss() {
        var revenue = jdbc.queryForObject(
                "SELECT COALESCE(SUM(e.credit_amount), 0) FROM acctg_trans_entry e " +
                "JOIN gl_account ga ON e.gl_account_id = ga.id JOIN acctg_trans t ON e.acctg_trans_id = t.id " +
                "WHERE ga.account_type = 'REVENUE' AND t.is_posted = true",
                java.math.BigDecimal.class);
        var cogs = jdbc.queryForObject(
                "SELECT COALESCE(SUM(e.debit_amount), 0) FROM acctg_trans_entry e " +
                "JOIN gl_account ga ON e.gl_account_id = ga.id JOIN acctg_trans t ON e.acctg_trans_id = t.id " +
                "WHERE ga.account_type = 'EXPENSE' AND t.is_posted = true",
                java.math.BigDecimal.class);
        var grossProfit = revenue.subtract(cogs);
        return Map.of("revenue", revenue, "cogs", cogs, "grossProfit", grossProfit);
    }

    @GetMapping("/reports/profit-per-item")
    public List<Map<String, Object>> profitPerItem() {
        return jdbc.queryForList(
                "SELECT p.name, p.packaging, p.sku, " +
                "COALESCE(SUM(CASE WHEN ga.account_code = '4000' THEN e.credit_amount ELSE 0 END), 0) as revenue, " +
                "COALESCE(SUM(CASE WHEN ga.account_code = '5000' THEN e.debit_amount ELSE 0 END), 0) as cogs, " +
                "COALESCE(SUM(CASE WHEN ga.account_code = '4000' THEN e.credit_amount ELSE 0 END), 0) - " +
                "COALESCE(SUM(CASE WHEN ga.account_code = '5000' THEN e.debit_amount ELSE 0 END), 0) as profit " +
                "FROM product p LEFT JOIN acctg_trans_entry e ON p.id = e.product_id " +
                "LEFT JOIN gl_account ga ON e.gl_account_id = ga.id " +
                "LEFT JOIN acctg_trans t ON e.acctg_trans_id = t.id AND t.is_posted = true " +
                "GROUP BY p.id, p.name, p.packaging, p.sku ORDER BY p.name"
        );
    }

    @GetMapping("/reports/inventory-valuation")
    public List<Map<String, Object>> inventoryValuation() {
        return jdbc.queryForList(
                "SELECT p.name, p.packaging, p.sku, " +
                "COALESCE(pc.avg_unit_cost, 0) as avg_cost, " +
                "COALESCE(pc.total_qty, 0) as qty, " +
                "COALESCE(pc.total_value, 0) as value " +
                "FROM product p LEFT JOIN product_costing pc ON p.id = pc.product_id " +
                "ORDER BY p.name"
        );
    }
}
