package com.wms.product;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    List<Product> findBySkuContainingIgnoreCase(String sku);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByActiveTrue();
    boolean existsBySku(String sku);
}
