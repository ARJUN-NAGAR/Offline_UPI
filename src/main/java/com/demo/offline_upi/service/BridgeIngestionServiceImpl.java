package com.demo.offline_upi.service;

import com.demo.offline_upi.crypto.HybridCryptoService;
import com.demo.offline_upi.dto.InboundPacketRequest;
import com.demo.offline_upi.dto.TransactionStatusResponse;
import com.demo.offline_upi.exception.DecryptionFailedException;
import com.demo.offline_upi.exception.DuplicatePacketException;
import com.demo.offline_upi.exception.TransactionReplayedException;
import com.demo.offline_upi.model.PaymentInstruction;
import com.demo.offline_upi.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Enterprise implementation of the bridge ingestion pipeline.
 * Performs sequential validation checks and triggers custom domain exception routing.
 */
@Service
public class BridgeIngestionServiceImpl implements BridgeIngestionService {

    private static final Logger log = LoggerFactory.getLogger(BridgeIngestionServiceImpl.class);

    @Autowired
    private HybridCryptoService crypto;

    @Autowired
    private IdempotencyService idempotency;

    @Autowired
    private SettlementService settlement;

    @Value("${upi.mesh.packet-max-age-seconds:86400}")
    private long maxAgeSeconds;

    @Override
    public TransactionStatusResponse ingest(InboundPacketRequest packet, String bridgeNodeId, int hopCount) {
        String packetHash;
        try {
            packetHash = crypto.hashCiphertext(packet.getCiphertext());
        } catch (Exception e) {
            throw new IllegalArgumentException("Hashing ciphertext failed: " + e.getMessage(), e);
        }

        // 1. Idempotency Gate
        if (!idempotency.claim(packetHash)) {
            log.warn("DUPLICATE packet {} from bridge {} — rejected",
                    packetHash.substring(0, 12) + "...", bridgeNodeId);
            throw new DuplicatePacketException("Packet with hash " + packetHash + " already processed");
        }

        // 2. Decryption & Integrity Validation
        PaymentInstruction instruction;
        try {
            instruction = crypto.decrypt(packet.getCiphertext());
        } catch (Exception e) {
            log.warn("Decryption failed for packet {}: {}",
                    packetHash.substring(0, 12) + "...", e.getMessage());
            throw new DecryptionFailedException("Failed to decrypt ciphertext payload: " + e.getMessage());
        }

        // 3. Freshness Check (Replay Protection)
        long ageSeconds = (Instant.now().toEpochMilli() - instruction.getSignedAt()) / 1000;
        if (ageSeconds > maxAgeSeconds) {
            log.warn("Packet {} too old ({}s), rejected",
                    packetHash.substring(0, 12) + "...", ageSeconds);
            throw new TransactionReplayedException("Packet violates maximum age limit: " + ageSeconds + " seconds");
        }
        if (ageSeconds < -300) { // small clock-skew tolerance
            throw new TransactionReplayedException("Packet timestamp is future-dated by " + (-ageSeconds) + " seconds");
        }

        // 4. Core Account Settlement
        Transaction tx = settlement.settle(instruction, packetHash, bridgeNodeId, hopCount);

        // Map outcome to clean Status Response
        return new TransactionStatusResponse(tx.getStatus().name(), packetHash, null, tx.getId());
    }
}
