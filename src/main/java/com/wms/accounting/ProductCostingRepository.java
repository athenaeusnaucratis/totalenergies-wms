package com.wms.accounting;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductCostingRepository extends JpaRepository<ProductCosting, Long> {
    Optional<ProductCosting> findByProductId(Long productId);
}
