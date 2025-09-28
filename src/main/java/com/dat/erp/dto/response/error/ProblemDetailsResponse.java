package com.dat.erp.dto.response.error;

import java.net.URI;
import java.time.LocalDateTime;

public record ProblemDetailsResponse(
        URI type, // A URI reference that identifies the problem type
        String title, // A short summary of the problem
        int status, // HTTP status code
        String detail, // Human-readable explanation
        String instance, // Request path
        LocalDateTime timestamp // (extra field, not in RFC but very useful)
) {
}
