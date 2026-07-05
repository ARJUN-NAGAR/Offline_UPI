package com.demo.offline_upi.dto;

import java.math.BigDecimal;

/**
 * Decoupled DTO exposing non-sensitive account details.
 */
public class AccountBalanceDto {

    private String vpa;
    private String holderName;
    private BigDecimal balance;

    public AccountBalanceDto() {}

    public AccountBalanceDto(String vpa, String holderName, BigDecimal balance) {
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
}
