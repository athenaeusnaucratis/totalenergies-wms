package com.wms.product;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplierPartRepository extends JpaRepository<SupplierPart, Long> {
    List<SupplierPart> findByProductId(Long productId);
    List<SupplierPart> findBySupplierId(Long supplierId);
}
