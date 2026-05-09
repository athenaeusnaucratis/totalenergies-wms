package com.wms.product;

import com.wms.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepo;

    public ProductCategoryService(ProductCategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    public List<ProductCategory> findAll() {
        return categoryRepo.findAll();
    }

    public List<ProductCategory> findRoots() {
        return categoryRepo.findByParentIsNull();
    }

    public List<ProductCategory> findChildren(Long parentId) {
        return categoryRepo.findByParentId(parentId);
    }

    public ProductCategory findById(Long id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", id));
    }

    public ProductCategory create(CategoryDto dto) {
        ProductCategory cat = new ProductCategory();
        cat.setName(dto.name());
        cat.setDescription(dto.description());
        if (dto.parentId() != null) {
            cat.setParent(findById(dto.parentId()));
        }
        return categoryRepo.save(cat);
    }

    public ProductCategory update(Long id, CategoryDto dto) {
        ProductCategory cat = findById(id);
        cat.setName(dto.name());
        cat.setDescription(dto.description());
        if (dto.parentId() != null) {
            cat.setParent(findById(dto.parentId()));
        } else {
            cat.setParent(null);
        }
        return categoryRepo.save(cat);
    }

    public void delete(Long id) {
        categoryRepo.deleteById(id);
    }

    public record CategoryDto(String name, String description, Long parentId) {}
}
