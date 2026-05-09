package com.wms.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {
    List<StockItem> findByProductId(Long productId);
    List<StockItem> findByLocationId(Long locationId);
    List<StockItem> findByStatus(StockItem.Status status);
    List<StockItem> findByProductIdAndStatus(Long productId, StockItem.Status status);
}
