package com.demo.offline_upi.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Permanent record of every settled/rejected transaction.
 * Unique constraint on packetHash guarantees idempotency at the database level.
 */
@Entity
@Table(name = "transactions",
       indexes = { @Index(name = "idx_packet_hash", columnList = "packet_hash", unique = true) })
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "packet_hash", nullable = false, unique = true, length = 64)
    private String packetHash; // SHA-256 hex of the encrypted packet

    @Column(name = "sender_vpa", nullable = false, length = 100)
    private String senderVpa;

    @Column(name = "receiver_vpa", nullable = false, length = 100)
    private String receiverVpa;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "signed_at", nullable = false)
    private Instant signedAt; // When the sender originally signed it (offline)

    @Column(name = "settled_at", nullable = false)
    private Instant settledAt; // When the backend actually processed it

    @Column(name = "bridge_node_id", nullable = false, length = 100)
    private String bridgeNodeId; // Which mesh node finally delivered it

    @Column(name = "hop_count", nullable = false)
    private int hopCount; // How many devices it passed through

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private Status status;

    public enum Status { SETTLED, REJECTED }

    public Transaction() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPacketHash() {
        return packetHash;
    }

    public void setPacketHash(String packetHash) {
        this.packetHash = packetHash;
    }

    public String getSenderVpa() {
        return senderVpa;
    }

    public void setSenderVpa(String senderVpa) {
        this.senderVpa = senderVpa;
    }

    public String getReceiverVpa() {
        return receiverVpa;
    }

    public void setReceiverVpa(String receiverVpa) {
        this.receiverVpa = receiverVpa;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Instant getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(Instant signedAt) {
        this.signedAt = signedAt;
    }

    public Instant getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(Instant settledAt) {
        this.settledAt = settledAt;
    }

    public String getBridgeNodeId() {
        return bridgeNodeId;
    }

    public void setBridgeNodeId(String bridgeNodeId) {
        this.bridgeNodeId = bridgeNodeId;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
