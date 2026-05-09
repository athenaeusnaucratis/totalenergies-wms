package com.wms.accounting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "acctg_trans_entry")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AcctgTransEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acctg_trans_id", nullable = false)
    @JsonIgnoreProperties({"entries", "hibernateLazyInitializer", "handler"})
    private AcctgTrans acctgTrans;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gl_account_id", nullable = false)
    @JsonIgnoreProperties({"parent", "hibernateLazyInitializer", "handler"})
    private GlAccount glAccount;

    @Column(name = "debit_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal debitAmount = BigDecimal.ZERO;

    @Column(name = "credit_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Column(name = "product_id")
    private Long productId;

    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public AcctgTrans getAcctgTrans() { return acctgTrans; }
    public void setAcctgTrans(AcctgTrans acctgTrans) { this.acctgTrans = acctgTrans; }
    public GlAccount getGlAccount() { return glAccount; }
    public void setGlAccount(GlAccount glAccount) { this.glAccount = glAccount; }
    public BigDecimal getDebitAmount() { return debitAmount; }
    public void setDebitAmount(BigDecimal debitAmount) { this.debitAmount = debitAmount; }
    public BigDecimal getCreditAmount() { return creditAmount; }
    public void setCreditAmount(BigDecimal creditAmount) { this.creditAmount = creditAmount; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
