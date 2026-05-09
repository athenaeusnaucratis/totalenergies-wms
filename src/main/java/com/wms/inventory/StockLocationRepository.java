package com.wms.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockLocationRepository extends JpaRepository<StockLocation, Long> {
    List<StockLocation> findByParentIsNull();
    List<StockLocation> findByParentId(Long parentId);
}
