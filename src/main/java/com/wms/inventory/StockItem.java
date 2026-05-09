package com.wms.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wms.common.BaseEntity;
import com.wms.product.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "stock_item")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StockItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id")
    @JsonIgnoreProperties({"parent", "hibernateLazyInitializer", "handler"})
    private StockLocation location;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "purchase_price", precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    private String batch;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.IN_STOCK;

    public enum Status { IN_STOCK, ALLOCATED, SHIPPED, DEPLETED }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public StockLocation getLocation() { return location; }
    public void setLocation(StockLocation location) { this.location = location; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
