package com.demo.offline_upi.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @Column(name = "vpa", length = 100, nullable = false)
    private String vpa;

    @Column(name = "holder_name", length = 150, nullable = false)
    private String holderName;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "pin_hash", nullable = false, length = 64)
    private String pinHash; // SHA-256 hash of authorized UPI PIN

    @Version
    @Column(name = "version")
    private Long version;

    public Account() {}

    public Account(String vpa, String holderName, BigDecimal balance, String pinHash) {
        this.vpa = vpa;
        this.holderName = holderName;
        this.balance = balance;
        this.pinHash = pinHash;
    }

    public String getVpa() { return vpa; }
    public void setVpa(String vpa) { this.vpa = vpa; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public boolean verifyPin(String providedPinHash) {
        return this.pinHash != null && this.pinHash.equalsIgnoreCase(providedPinHash);
    }
}