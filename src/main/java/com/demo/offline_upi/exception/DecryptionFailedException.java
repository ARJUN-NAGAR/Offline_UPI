package com.demo.offline_upi.exception;

/**
 * Thrown when hybrid decryption or integrity checks fail.
 */
public class DecryptionFailedException extends RuntimeException {
    public DecryptionFailedException(String message) {
        super(message);
    }
}
