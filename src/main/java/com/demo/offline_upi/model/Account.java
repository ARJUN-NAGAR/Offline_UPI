package com.demo.offline_upi.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Simulated bank account entity.
 * Uses @Version for optimistic locking to handle concurrent updates securely.
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @Column(name = "vpa", length = 100, nullable = false)
    private String vpa; // Virtual Payment Address, e.g. "alice@demo"

    @Column(name = "holder_name", length = 150, nullable = false)
    private String holderName;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Version
    @Column(name = "version")
    private Long version;

    public Account() {}

    public Account(String vpa, String holderName, BigDecimal balance) {
        this.vpa = vpa;
        this.holderName = holderName;
        this.balance = balance;
    }

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
