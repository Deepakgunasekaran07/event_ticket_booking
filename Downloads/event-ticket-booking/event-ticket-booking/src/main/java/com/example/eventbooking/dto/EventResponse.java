package com.example.eventbooking.dto;

import com.example.eventbooking.entity.Event;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EventResponse(Integer id, String name, String venue, String category,
                            LocalDate event_date, Integer total_seats, BigDecimal base_price) {
    public static EventResponse from(Event e) {
        return new EventResponse(e.getId(), e.getName(), e.getVenue(), e.getCategory(),
                e.getEventDate(), e.getTotalSeats(), e.getBasePrice());
    }
}
