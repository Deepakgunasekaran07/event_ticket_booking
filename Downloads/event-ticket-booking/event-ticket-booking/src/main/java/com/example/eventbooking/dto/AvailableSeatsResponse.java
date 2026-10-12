package com.example.eventbooking.dto;

import java.util.List;

public record AvailableSeatsResponse(Integer event_id, Integer total_seats,
                                     List<Integer> available_seats, Integer seats_available) {}
