package com.wms.purchasing;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
    List<GoodsReceipt> findByPurchaseOrderId(Long poId);
}
