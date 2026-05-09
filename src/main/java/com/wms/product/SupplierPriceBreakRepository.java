package com.wms.product;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplierPriceBreakRepository extends JpaRepository<SupplierPriceBreak, Long> {
    List<SupplierPriceBreak> findBySupplierPartIdOrderByQuantityAsc(Long supplierPartId);
}
