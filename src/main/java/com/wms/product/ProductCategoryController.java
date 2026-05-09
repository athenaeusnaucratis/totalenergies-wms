package com.wms.product;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-categories")
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    public ProductCategoryController(ProductCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<ProductCategory> list(@RequestParam(required = false) Long parentId) {
        if (parentId != null) return categoryService.findChildren(parentId);
        return categoryService.findAll();
    }

    @GetMapping("/roots")
    public List<ProductCategory> roots() {
        return categoryService.findRoots();
    }

    @GetMapping("/{id}")
    public ProductCategory get(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ProductCategory> create(@Valid @RequestBody ProductCategoryService.CategoryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(dto));
    }

    @PutMapping("/{id}")
    public ProductCategory update(@PathVariable Long id, @Valid @RequestBody ProductCategoryService.CategoryDto dto) {
        return categoryService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
