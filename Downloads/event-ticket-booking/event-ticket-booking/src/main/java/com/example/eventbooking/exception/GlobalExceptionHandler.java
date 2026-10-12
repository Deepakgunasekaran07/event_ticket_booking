package com.example.eventbooking.exception;

import com.example.eventbooking.dto.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> Map.of("field", fe.getField(), "message", Optional.ofNullable(fe.getDefaultMessage()).orElse("invalid value")))
            .distinct().toList();
        return response(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return response(HttpStatus.BAD_REQUEST, "Invalid value for " + ex.getName(),
                List.of(Map.of("field", ex.getName(), "message", "must be a valid value of the required type")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return response(HttpStatus.BAD_REQUEST, "Malformed JSON or invalid field format",
                List.of(Map.of("field", "request", "message", "request body must be valid JSON with correctly formatted values")));
    }

    @ExceptionHandler(DuplicateEventException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(DuplicateEventException ex) {
        return response(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(BookingRequestValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleBookingValidation(BookingRequestValidationException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getErrors());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleIntegrity(DataIntegrityViolationException ex) {
        return response(HttpStatus.CONFLICT, "A database uniqueness or integrity rule was violated", List.of());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraint(ConstraintViolationException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleOther(Exception ex) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred", List.of());
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String message, List<Map<String, String>> fields) {
        ApiErrorResponse body = new ApiErrorResponse(LocalDateTime.now(), status.value(),
                status.getReasonPhrase(), message, fields);
        return ResponseEntity.status(status).body(body);
    }
}
