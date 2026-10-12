package com.example.eventbooking.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record BookingRequest(
    @NotNull(message = "is required") @Positive(message = "must be a positive integer") Integer event_id,
    @NotBlank(message = "must not be blank") @Email(message = "must be a valid email address")
    @Size(max = 254, message = "must be at most 254 characters") String customer_email,
    @NotNull(message = "is required") @Size(min = 1, max = 6, message = "must contain between 1 and 6 seat numbers")
    List<@NotNull(message = "must not be null") @Positive(message = "must be a positive integer") Integer> seat_numbers,
    @NotNull(message = "is required") @Min(value = 1, message = "must be at least 1") Integer seats
) {}
