package com.dat.erp.exceptions;

/**
 * Thrown when authentication is required and has failed or not yet been provided.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

