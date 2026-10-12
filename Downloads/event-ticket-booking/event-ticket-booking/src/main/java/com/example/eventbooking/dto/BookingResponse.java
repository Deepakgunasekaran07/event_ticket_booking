package com.example.eventbooking.dto;

import com.example.eventbooking.entity.Booking;
import com.example.eventbooking.entity.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(Integer id, Integer event_id, String customer_email,
                              List<Integer> seat_number, Integer seats,
                              BigDecimal price_per_seat, BigDecimal total_amount,
                              BookingStatus status, String rejection_reason, LocalDateTime booking_on) {
    public static BookingResponse from(Booking b) {
        return new BookingResponse(b.getId(), b.getEvent().getId(), b.getCustomerEmail(),
                List.copyOf(b.getSeatNumber()), b.getSeats(), b.getPricePerSeat(),
                b.getTotalAmount(), b.getStatus(), b.getRejectionReason(), b.getBookingOn());
    }
}
