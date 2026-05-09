package com.wms.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wms.common.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "supplier_price_break")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SupplierPriceBreak extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_part_id", nullable = false)
    private SupplierPart supplierPart;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    public SupplierPart getSupplierPart() { return supplierPart; }
    public void setSupplierPart(SupplierPart supplierPart) { this.supplierPart = supplierPart; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
