package com.demo.offline_upi.dto;

/**
 * Decoupled response DTO mapping transaction status details back to the bridge.
 */
public class TransactionStatusResponse {

    private String outcome;     // SETTLED, DUPLICATE_DROPPED, INVALID
    private String packetHash;
    private String reason;      // Decryption failure, stale packet, etc.
    private Long transactionId; // ID of the transaction if settled

    public TransactionStatusResponse() {}

    public TransactionStatusResponse(String outcome, String packetHash, String reason, Long transactionId) {
        this.outcome = outcome;
        this.packetHash = packetHash;
        this.reason = reason;
        this.transactionId = transactionId;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getPacketHash() {
        return packetHash;
    }

    public void setPacketHash(String packetHash) {
        this.packetHash = packetHash;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }
}
