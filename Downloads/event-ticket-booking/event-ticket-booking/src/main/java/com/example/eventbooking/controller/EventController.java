package com.example.eventbooking.controller;

import com.example.eventbooking.dto.*;
import com.example.eventbooking.entity.BookingStatus;
import com.example.eventbooking.entity.Event;
import com.example.eventbooking.service.BookingService;
import com.example.eventbooking.service.EventService;
import com.example.eventbooking.service.EventSummaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api")
public class EventController {
    private final EventService eventService;
    private final BookingService bookingService;
    private final EventSummaryService summaryService;

    public EventController(EventService eventService, BookingService bookingService, EventSummaryService summaryService) {
        this.eventService = eventService;
        this.bookingService = bookingService;
        this.summaryService = summaryService;
    }

    @PostMapping("/events")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        Event created = eventService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(created));
    }

    @GetMapping("/events/{id}")
    public EventResponse getEvent(@PathVariable Integer id) {
        return EventResponse.from(eventService.get(id));
    }

    @GetMapping("/events/{id}/bookings")
    public List<BookingResponse> listBookings(@PathVariable Integer id, @RequestParam(required = false) String status) {
        BookingStatus parsed = null;
        if (status != null) {
            try {
                parsed = BookingStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new com.example.eventbooking.exception.BadRequestException("status must be CONFIRMED or REJECTED");
            }
        }
        return bookingService.listForEvent(id, parsed).stream().map(BookingResponse::from).toList();
    }

    @GetMapping("/events/{id}/seats")
    public AvailableSeatsResponse availableSeats(@PathVariable Integer id) {
        Event event = bookingService.eventOr404(id);
        List<Integer> seats = bookingService.availableSeats(id);
        return new AvailableSeatsResponse(id, event.getTotalSeats(), seats, seats.size());
    }

    @GetMapping({"/events/{id}/summary", "/events/{id}/summery"})
    public EventSummaryResponse summary(@PathVariable Integer id) {
        return summaryService.getSummary(id);
    }
}
