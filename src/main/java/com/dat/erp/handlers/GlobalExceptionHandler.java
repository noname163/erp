package com.dat.erp.handlers;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.dat.erp.dtos.responses.error.ProblemDetailsResponse;
import com.dat.erp.dtos.responses.error.ValidationProblemDetailsReponse;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ResourceNotFoundException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetailsResponse> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {} at {}", ex.getMessage(), request.getDescription(false));
        ProblemDetailsResponse problem = new ProblemDetailsResponse(
                URI.create("https://example.com/errors/not-found"),
                "Resource Not Found",
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ProblemDetailsResponse> handleBadRequest(BadRequestException ex, WebRequest request) {
        log.warn("Resource not found: {} at {}", ex.getMessage(), request.getDescription(false));
        ProblemDetailsResponse problem = new ProblemDetailsResponse(
                URI.create("https://example.com/errors/bad-request"),
                "Bad Request",
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(Exception.class) // fallback
    public ResponseEntity<ProblemDetailsResponse> handleGeneric(Exception ex, WebRequest request) {
        log.warn("Resource not found: {} at {}", ex.getMessage(), request.getDescription(false));
        ProblemDetailsResponse problem = new ProblemDetailsResponse(
                URI.create("https://example.com/errors/internal"),
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                request.getDescription(false).replace("uri=", ""),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationProblemDetailsReponse> handleValidationErrors(MethodArgumentNotValidException ex,
            WebRequest request) {
        log.warn("Resource not found: {} at {}", ex.getMessage(), request.getDescription(false));
        List<ValidationProblemDetailsReponse.FieldError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> new ValidationProblemDetailsReponse.FieldError(err.getField(), err.getDefaultMessage()))
                .collect(Collectors.toList());

        ValidationProblemDetailsReponse problem = new ValidationProblemDetailsReponse(
                URI.create("https://example.com/errors/validation"),
                "Validation Failed",
                HttpStatus.BAD_REQUEST.value(),
                "One or more fields are invalid",
                request.getDescription(false).replace("uri=", ""),
                LocalDateTime.now(),
                errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    // ✅ Handle @Validated on query params/path params
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationProblemDetailsReponse> handleConstraintViolations(ConstraintViolationException ex,
            WebRequest request) {
        log.warn("Resource not found: {} at {}", ex.getMessage(), request.getDescription(false));
        List<ValidationProblemDetailsReponse.FieldError> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> new ValidationProblemDetailsReponse.FieldError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .collect(Collectors.toList());

        ValidationProblemDetailsReponse problem = new ValidationProblemDetailsReponse(
                URI.create("https://example.com/errors/validation"),
                "Validation Failed",
                HttpStatus.BAD_REQUEST.value(),
                "One or more parameters are invalid",
                request.getDescription(false).replace("uri=", ""),
                LocalDateTime.now(),
                errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }
}
