package com.wms.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wms.common.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "product")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String sku;

    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"parent", "hibernateLazyInitializer"})
    private ProductCategory category;

    @Column(nullable = false)
    private String packaging = "";

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Column(name = "selling_price", precision = 12, scale = 2)
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit = Unit.EACH;

    @Column(name = "minimum_stock")
    private Integer minimumStock = 0;

    @Column(nullable = false)
    private boolean active = true;

    public enum Unit { EACH, KG, L, M, BOX }

    public BigDecimal getPriceDifference() {
        if (sellingPrice != null && costPrice != null) {
            return sellingPrice.subtract(costPrice);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getMarginPercent() {
        if (sellingPrice != null && costPrice != null && sellingPrice.compareTo(BigDecimal.ZERO) > 0) {
            return sellingPrice.subtract(costPrice)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(sellingPrice, 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ProductCategory getCategory() { return category; }
    public void setCategory(ProductCategory category) { this.category = category; }
    public String getPackaging() { return packaging; }
    public void setPackaging(String packaging) { this.packaging = packaging; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }
    public Integer getMinimumStock() { return minimumStock; }
    public void setMinimumStock(Integer minimumStock) { this.minimumStock = minimumStock; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
