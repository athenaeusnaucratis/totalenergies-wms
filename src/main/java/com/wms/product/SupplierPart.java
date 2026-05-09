package com.wms.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wms.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "supplier_part", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "supplier_id"}))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SupplierPart extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Company supplier;

    @Column(name = "supplier_sku")
    private String supplierSku;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Company getSupplier() { return supplier; }
    public void setSupplier(Company supplier) { this.supplier = supplier; }
    public String getSupplierSku() { return supplierSku; }
    public void setSupplierSku(String supplierSku) { this.supplierSku = supplierSku; }
    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }
}
