package com.wms.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wms.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "product_category")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProductCategory extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties({"parent", "hibernateLazyInitializer"})
    private ProductCategory parent;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ProductCategory getParent() { return parent; }
    public void setParent(ProductCategory parent) { this.parent = parent; }
}
