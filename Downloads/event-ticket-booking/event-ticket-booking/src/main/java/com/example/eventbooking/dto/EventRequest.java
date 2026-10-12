package com.example.eventbooking.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EventRequest(
    @NotBlank(message = "must not be blank") @Size(max = 150, message = "must be at most 150 characters")
    String name,
    @NotBlank(message = "must not be blank") @Size(max = 200, message = "must be at most 200 characters")
    String venue,
    @NotBlank(message = "must not be blank") @Size(max = 100, message = "must be at most 100 characters")
    String category,
    @NotNull(message = "is required") LocalDate event_date,
    @NotNull(message = "is required") @Min(value = 1, message = "must be at least 1")
    @Max(value = 1000000, message = "must be at most 1000000") Integer total_seats,
    @NotNull(message = "is required") @DecimalMin(value = "0.01", message = "must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "must have at most 2 decimal places") BigDecimal base_price
) {}
