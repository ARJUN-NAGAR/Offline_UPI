package com.demo.offline_upi.service;

/**
 * Interface defining idempotency caching operations.
 */
public interface IdempotencyService {
    boolean claim(String packetHash);
    int size();
    void evictExpired();
    void clear();
}
