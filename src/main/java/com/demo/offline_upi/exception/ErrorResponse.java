package com.demo.offline_upi.exception;

import java.time.Instant;

/**
 * Standardized error payload returned by GlobalExceptionHandler on errors.
 */
public class ErrorResponse {

    private Instant timestamp;
    private String errorCode;
    private String message;

    public ErrorResponse() {
        this.timestamp = Instant.now();
    }

    public ErrorResponse(String errorCode, String message) {
        this.timestamp = Instant.now();
        this.errorCode = errorCode;
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
