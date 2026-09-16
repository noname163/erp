package com.dat.erp.handlers;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.dat.erp.dto.response.error.ProblemDetailsResponse;
import com.dat.erp.dto.response.error.ValidationProblemDetailsReponse;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ConflictException;
import com.dat.erp.exceptions.ForbiddenException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.exceptions.UnauthorizedException;
import com.dat.erp.exceptions.UnprocessableEntityException;

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
        log.warn("Bad request: {} at {}", ex.getMessage(), request.getDescription(false));
        ProblemDetailsResponse problem = new ProblemDetailsResponse(
                URI.create("https://example.com/errors/bad-request"),
                "Bad Request",
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ProblemDetailsResponse> handleUnauthorized(UnauthorizedException ex, WebRequest request) {
        log.warn("Unauthorized: {} at {}", ex.getMessage(), request.getDescription(false));
        ProblemDetailsResponse problem = new ProblemDetailsResponse(
                URI.create("https://example.com/errors/unauthorized"),
                "Unauthorized",
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ProblemDetailsResponse> handleForbidden(ForbiddenException ex, WebRequest request) {
        log.warn("Forbidden: {} at {}", ex.getMessage(), request.getDescription(false));
        ProblemDetailsResponse problem = new ProblemDetailsResponse(
                URI.create("https://example.com/errors/forbidden"),
                "Forbidden",
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetailsResponse> handleConflict(ConflictException ex, WebRequest request) {
        log.warn("Conflict: {} at {}", ex.getMessage(), request.getDescription(false));
        ProblemDetailsResponse problem = new ProblemDetailsResponse(
                URI.create("https://example.com/errors/conflict"),
                "Conflict",
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ProblemDetailsResponse> handleUnprocessable(UnprocessableEntityException ex,
            WebRequest request) {
        log.warn("Unprocessable entity: {} at {}", ex.getMessage(), request.getDescription(false));
        ProblemDetailsResponse problem = new ProblemDetailsResponse(
                URI.create("https://example.com/errors/unprocessable-entity"),
                "Unprocessable Entity",
                422,
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                LocalDateTime.now());
        return ResponseEntity.status(422).body(problem);
    }

    @ExceptionHandler(Exception.class) // fallback
    public ResponseEntity<ProblemDetailsResponse> handleGeneric(Exception ex, WebRequest request) {
        log.error("Unhandled exception at {}", request.getDescription(false), ex);
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
        log.warn("Validation failed: {} at {}", ex.getMessage(), request.getDescription(false));
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

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ProblemDetailsResponse> handleAuthorizationDeniedException(
            AuthorizationDeniedException ex,
            WebRequest request) {

        log.warn("Authorization denied: {} at {}", ex.getMessage(), request.getDescription(false));

        ProblemDetailsResponse problem = new ProblemDetailsResponse(
                URI.create("https://example.com/errors/forbidden"),
                "Forbidden",
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    // ✅ Handle @Validated on query params/path params
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationProblemDetailsReponse> handleConstraintViolations(ConstraintViolationException ex,
            WebRequest request) {
        log.warn("Constraint violations: {} at {}", ex.getMessage(), request.getDescription(false));
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
