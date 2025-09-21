package com.dat.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
