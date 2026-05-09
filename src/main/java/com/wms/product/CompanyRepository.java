package com.wms.product;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findBySupplierTrue();
    List<Company> findByCustomerTrue();
}
