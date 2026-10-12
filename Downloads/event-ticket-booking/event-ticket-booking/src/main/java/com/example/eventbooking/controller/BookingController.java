package com.example.eventbooking.controller;

import com.example.eventbooking.dto.BookingRequest;
import com.example.eventbooking.dto.BookingResponse;
import com.example.eventbooking.entity.Booking;
import com.example.eventbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BookingController {
    private final BookingService bookingService;
    public BookingController(BookingService bookingService) { this.bookingService = bookingService; }

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        Booking booking = bookingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }

    @GetMapping("/booking/{id}")
    public BookingResponse getBooking(@PathVariable Integer id) {
        return BookingResponse.from(bookingService.get(id));
    }
}
