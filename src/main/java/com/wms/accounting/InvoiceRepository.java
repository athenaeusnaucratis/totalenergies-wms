package com.wms.accounting;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByInvoiceType(Invoice.InvoiceType type);
    List<Invoice> findByStatus(Invoice.InvoiceStatus status);
    List<Invoice> findAllByOrderByCreatedAtDesc();
}
