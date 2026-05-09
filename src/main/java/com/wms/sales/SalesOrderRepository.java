package com.wms.sales;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    List<SalesOrder> findByStatus(SalesOrder.Status status);
    List<SalesOrder> findAllByOrderByCreatedAtDesc();
}
