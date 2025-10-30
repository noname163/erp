package com.dat.erp.exceptions;

/**
 * Thrown when the user is authenticated but does not have permission to access the resource.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}

