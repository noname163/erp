package com.dat.erp.builders;

import org.springframework.http.ResponseEntity;

import com.dat.erp.dtos.responses.ApiResponse;

public class ResponseBuilder {
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(new ApiResponse<>(true, "OK", data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "Created", data));
    }
}
