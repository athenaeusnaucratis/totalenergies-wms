package com.wms.accounting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wms.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "gl_account")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GlAccount extends BaseEntity {

    @Column(name = "account_code", nullable = false, unique = true)
    private String accountCode;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties({"parent", "hibernateLazyInitializer", "handler"})
    private GlAccount parent;

    public enum AccountType { ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE }

    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public GlAccount getParent() { return parent; }
    public void setParent(GlAccount parent) { this.parent = parent; }
}
