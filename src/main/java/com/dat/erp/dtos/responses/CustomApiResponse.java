package com.dat.erp.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
