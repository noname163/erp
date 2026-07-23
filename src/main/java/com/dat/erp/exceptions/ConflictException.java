package com.dat.erp.exceptions;

/**
 * Thrown when a request conflicts with the current state of the server (e.g., duplicate resource).
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}

