package com.wms.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wms.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "stock_location")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StockLocation extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties({"parent", "hibernateLazyInitializer", "handler"})
    private StockLocation parent;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public StockLocation getParent() { return parent; }
    public void setParent(StockLocation parent) { this.parent = parent; }
}
