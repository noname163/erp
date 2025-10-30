package com.dat.erp.exceptions;

/**
 * Thrown when the server understands the content type and syntax of the request entity,
 * but was unable to process the contained instructions (semantic validation errors).
 */
public class UnprocessableEntityException extends RuntimeException {
    public UnprocessableEntityException(String message) {
        super(message);
    }
}

