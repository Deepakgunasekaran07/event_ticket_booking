package com.example.eventbooking.exception;

import java.util.List;
import java.util.Map;

public class BookingRequestValidationException extends BadRequestException {
    private final List<Map<String, String>> errors;
    public BookingRequestValidationException(List<Map<String, String>> errors) {
        super("Booking request validation failed");
        this.errors = errors;
    }
    public List<Map<String, String>> getErrors() { return errors; }
}
