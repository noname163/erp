package com.dat.erp.builders;

import org.springframework.http.ResponseEntity;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.response.CustomApiResponse;

/**
 * Helper to build consistent API responses.
 */
public class ResponseBuilder {
    public static <T> ResponseEntity<CustomApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(new CustomApiResponse<>(true, Messages.OK, data));
    }

    public static <T> ResponseEntity<CustomApiResponse<T>> created(T data) {
        return ResponseEntity.status(201).body(new CustomApiResponse<>(true, Messages.CREATED, data));
    }
}
