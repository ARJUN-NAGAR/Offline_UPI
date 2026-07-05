package com.demo.offline_upi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centrally intercepts custom exceptions and translates them into standardised ErrorResponse messages.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DecryptionFailedException.class)
    public ResponseEntity<ErrorResponse> handleDecryptionFailed(DecryptionFailedException ex) {
        ErrorResponse error = new ErrorResponse("DECRYPTION_FAILED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DuplicatePacketException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePacket(DuplicatePacketException ex) {
        ErrorResponse error = new ErrorResponse("DUPLICATE_PACKET", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(TransactionReplayedException.class)
    public ResponseEntity<ErrorResponse> handleTransactionReplayed(TransactionReplayedException ex) {
        ErrorResponse error = new ErrorResponse("TRANSACTION_REPLAYED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_ARGUMENT", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ErrorResponse error = new ErrorResponse("INTERNAL_SERVER_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
