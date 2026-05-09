package com.wms.purchasing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wms.inventory.StockItem;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "goods_receipt_line")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GoodsReceiptLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    @JsonIgnoreProperties({"lines", "hibernateLazyInitializer", "handler"})
    private GoodsReceipt goodsReceipt;

    @Column(name = "po_line_id", nullable = false)
    private Long poLineId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stock_item_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private StockItem stockItem;

    @Column(name = "quantity_received", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantityReceived;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public GoodsReceipt getGoodsReceipt() { return goodsReceipt; }
    public void setGoodsReceipt(GoodsReceipt goodsReceipt) { this.goodsReceipt = goodsReceipt; }
    public Long getPoLineId() { return poLineId; }
    public void setPoLineId(Long poLineId) { this.poLineId = poLineId; }
    public StockItem getStockItem() { return stockItem; }
    public void setStockItem(StockItem stockItem) { this.stockItem = stockItem; }
    public BigDecimal getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(BigDecimal quantityReceived) { this.quantityReceived = quantityReceived; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
