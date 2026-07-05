package com.demo.offline_upi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Decoupled request DTO representing the over-the-wire MeshPacket format.
 * Validates incoming parameters before service ingestion.
 */
public class InboundPacketRequest {

    @NotBlank(message = "Packet ID cannot be blank")
    private String packetId;

    @Min(value = 0, message = "TTL cannot be negative")
    private int ttl;

    @NotNull(message = "CreatedAt timestamp is required")
    private Long createdAt;

    @NotBlank(message = "Ciphertext cannot be blank")
    private String ciphertext;

    public InboundPacketRequest() {}

    public InboundPacketRequest(String packetId, int ttl, Long createdAt, String ciphertext) {
        this.packetId = packetId;
        this.ttl = ttl;
        this.createdAt = createdAt;
        this.ciphertext = ciphertext;
    }

    public String getPacketId() {
        return packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public String getCiphertext() {
        return ciphertext;
    }

    public void setCiphertext(String ciphertext) {
        this.ciphertext = ciphertext;
    }
}
