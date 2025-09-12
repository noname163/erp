package com.dat.erp.dtos.responses.error;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

public record ValidationProblemDetailsReponse(
        URI type,
        String title,
        int status,
        String detail,
        String instance,
        LocalDateTime timestamp,
        List<FieldError> errors) {
    public record FieldError(String field, String message) {
    }
}