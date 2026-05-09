package com.wms.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockItemTrackingRepository extends JpaRepository<StockItemTracking, Long> {
    List<StockItemTracking> findByStockItemIdOrderByCreatedAtDesc(Long stockItemId);
}
