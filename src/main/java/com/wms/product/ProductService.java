package com.wms.product;

import com.wms.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepo;
    private final ProductCategoryRepository categoryRepo;

    public ProductService(ProductRepository productRepo, ProductCategoryRepository categoryRepo) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
    }

    public List<Product> findAll() {
        return productRepo.findAll();
    }

    public List<Product> findByCategory(Long categoryId) {
        return productRepo.findByCategoryId(categoryId);
    }

    public Product findById(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    public List<Product> findBySku(String sku) {
        List<Product> results = productRepo.findBySkuContainingIgnoreCase(sku);
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("Product with SKU " + sku, 0L);
        }
        return results;
    }

    public Product create(ProductDto dto) {
        Product p = new Product();
        applyDto(p, dto);
        return productRepo.save(p);
    }

    public Product update(Long id, ProductDto dto) {
        Product p = findById(id);
        applyDto(p, dto);
        return productRepo.save(p);
    }

    public void delete(Long id) {
        productRepo.deleteById(id);
    }

    private void applyDto(Product p, ProductDto dto) {
        p.setName(dto.name());
        p.setSku(dto.sku());
        p.setDescription(dto.description());
        p.setPackaging(dto.packaging() != null ? dto.packaging() : "");
        p.setCostPrice(dto.costPrice() != null ? dto.costPrice() : BigDecimal.ZERO);
        p.setSellingPrice(dto.sellingPrice() != null ? dto.sellingPrice() : BigDecimal.ZERO);
        p.setUnit(dto.unit() != null ? dto.unit() : Product.Unit.EACH);
        p.setMinimumStock(dto.minimumStock() != null ? dto.minimumStock() : 0);
        p.setActive(dto.active() != null ? dto.active() : true);
        if (dto.categoryId() != null) {
            p.setCategory(categoryRepo.findById(dto.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", dto.categoryId())));
        } else {
            p.setCategory(null);
        }
    }

    public record ProductDto(String name, String sku, String description, String packaging,
                             BigDecimal costPrice, BigDecimal sellingPrice, Long categoryId,
                             Product.Unit unit, Integer minimumStock, Boolean active) {}
}
