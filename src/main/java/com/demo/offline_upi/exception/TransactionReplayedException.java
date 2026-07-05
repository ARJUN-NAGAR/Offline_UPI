package com.demo.offline_upi.exception;

/**
 * Thrown when a packet's timestamp violates the freshness window config.
 */
public class TransactionReplayedException extends RuntimeException {
    public TransactionReplayedException(String message) {
        super(message);
    }
}
