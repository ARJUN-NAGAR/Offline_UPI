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
        try {
            // Seed accounts with pre-configured hashed PINs
            accounts.save(new Account("alice@demo", "Alice Smith", new BigDecimal("500.00"), hashPin("1234")));
            accounts.save(new Account("bob@demo", "Bob Jones", new BigDecimal("300.00"), hashPin("2345")));
            accounts.save(new Account("charlie@demo", "Charlie Brown", new BigDecimal("1000.00"), hashPin("3456")));
            log.info("Demo database seeded with 3 accounts and secure PIN hashes.");
        } catch (Exception e) {
            log.error("Failed to seed demo data", e);
        }
    }

    @Override
    public InboundPacketRequest createSignedTransaction(String sender, String receiver,
                                                        BigDecimal amount, String pin, int ttl) throws Exception {
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