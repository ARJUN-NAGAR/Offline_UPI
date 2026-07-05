package com.demo.offline_upi.service;

import com.demo.offline_upi.crypto.HybridCryptoService;
import com.demo.offline_upi.crypto.ServerKeyHolder;
import com.demo.offline_upi.dto.InboundPacketRequest;
import com.demo.offline_upi.model.Account;
import com.demo.offline_upi.model.PaymentInstruction;
import com.demo.offline_upi.repository.AccountRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

/**
 * Implementation of DemoService to seed database and generate valid encrypted payloads.
 */
@Service
public class DemoServiceImpl implements DemoService {

    private static final Logger log = LoggerFactory.getLogger(DemoServiceImpl.class);

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private ServerKeyHolder keyHolder;

    @Autowired
    private HybridCryptoService crypto;

    @PostConstruct
    @Override
    public void initData() {
        accounts.save(new Account("alice@demo", "Alice Smith", new BigDecimal("500.00")));
        accounts.save(new Account("bob@demo", "Bob Jones", new BigDecimal("300.00")));
        accounts.save(new Account("charlie@demo", "Charlie Brown", new BigDecimal("1000.00")));
        log.info("Demo database seeded with 3 accounts.");
    }

    @Override
    public InboundPacketRequest createSignedTransaction(String sender, String receiver,
                                                        BigDecimal amount, String pin, int ttl) throws Exception {
        // In real UPI Lite/Offline, the client app has a pre-loaded Server Public Key.
        // It encrypts the payload offline. We simulate that here.
        PaymentInstruction ins = new PaymentInstruction();
        ins.setSenderVpa(sender);
        ins.setReceiverVpa(receiver);
        ins.setAmount(amount);
        ins.setPinHash(hashPin(pin));
        ins.setNonce(UUID.randomUUID().toString());
        ins.setSignedAt(Instant.now().toEpochMilli());

        String ciphertext = crypto.encrypt(ins, keyHolder.getPublicKey());

        return new InboundPacketRequest(
                UUID.randomUUID().toString(),
                ttl,
                Instant.now().toEpochMilli(),
                ciphertext
        );
    }

    private String hashPin(String pin) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] hash = sha.digest(pin.getBytes());
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
