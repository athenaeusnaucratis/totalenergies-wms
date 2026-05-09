package com.wms.sales;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalesOrderAllocationRepository extends JpaRepository<SalesOrderAllocation, Long> {
    List<SalesOrderAllocation> findBySoLineId(Long soLineId);
}
