package com.demo.offline_upi.service;

import com.demo.offline_upi.dto.InboundPacketRequest;

import java.math.BigDecimal;

/**
 * Interface defining demo utilities for database seeding and offline transaction signing.
 */
public interface DemoService {
    void initData();
    InboundPacketRequest createSignedTransaction(String sender, String receiver, BigDecimal amount, String pin, int ttl) throws Exception;
}
