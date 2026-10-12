package com.example.eventbooking.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ApiErrorResponse(LocalDateTime timestamp, int status, String error,
                               String message, List<Map<String, String>> field_errors) {}
