package com.wms.sales;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wms.common.BaseEntity;
import com.wms.product.Company;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_order")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SalesOrder extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Company customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate = LocalDate.now();

    private String notes;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"salesOrder", "hibernateLazyInitializer", "handler"})
    private List<SalesOrderLine> lines = new ArrayList<>();

    public enum Status { PENDING, CONFIRMED, ALLOCATED, SHIPPED, COMPLETE, CANCELLED }

    public Company getCustomer() { return customer; }
    public void setCustomer(Company customer) { this.customer = customer; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<SalesOrderLine> getLines() { return lines; }
    public void setLines(List<SalesOrderLine> lines) { this.lines = lines; }
}
