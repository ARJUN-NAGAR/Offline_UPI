package com.demo.offline_upi.exception;

/**
 * Thrown when a packet fails the idempotency claim check.
 */
public class DuplicatePacketException extends RuntimeException {
    public DuplicatePacketException(String message) {
        super(message);
    }
}
