package com.example.eventbooking.service;

import com.example.eventbooking.dto.BookingRequest;
import com.example.eventbooking.entity.*;
import com.example.eventbooking.exception.BadRequestException;
import com.example.eventbooking.exception.BookingRequestValidationException;
import com.example.eventbooking.exception.ResourceNotFoundException;
import com.example.eventbooking.repository.BookingRepository;
import com.example.eventbooking.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;

    public BookingService(BookingRepository bookingRepository, EventRepository eventRepository) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Booking create(BookingRequest request) {
        Integer eventId = request.event_id();
        if (eventId == null || eventId <= 0) throw new BadRequestException("event_id must be a positive integer");

        List<Integer> requested = request.seat_numbers();
        List<Map<String, String>> errors = new ArrayList<>();
        if (requested != null && new HashSet<>(requested).size() != requested.size()) {
            errors.add(Map.of("field", "seat_numbers", "message", "must not contain duplicate seat numbers"));
        }
        if (requested != null && request.seats() != null && request.seats() != requested.size()) {
            errors.add(Map.of("field", "seats", "message", "must equal the number of items in seat_numbers"));
        }
        if (!errors.isEmpty()) throw new BookingRequestValidationException(errors);

        // Locking the event row serializes booking checks for the same event.
        Event event = eventRepository.findByIdForUpdate(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event " + eventId + " was not found"));

        for (Integer seat : requested) {
            if (seat != null && seat > event.getTotalSeats()) {
                throw new BookingRequestValidationException(List.of(Map.of("field", "seat_number", "message", "seat " + seat + " does not exist for this event")));
            }
        }

        BigDecimal base = event.getBasePrice();
        BigDecimal discount = BigDecimal.ZERO;
        if (!event.getEventDate().isBefore(LocalDate.now().plusDays(30))) {
            discount = discount.add(new BigDecimal("0.10"));
        }
        if (requested.size() >= 4) discount = discount.add(new BigDecimal("0.05"));
        BigDecimal pricePerSeat = base.multiply(BigDecimal.ONE.subtract(discount)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = pricePerSeat.multiply(BigDecimal.valueOf(requested.size())).setScale(2, RoundingMode.HALF_UP);

        Booking booking = new Booking();
        booking.setEvent(event);
        booking.setCustomerEmail(request.customer_email().trim().toLowerCase(Locale.ROOT));
        booking.setSeatNumber(new ArrayList<>(requested));
        booking.setSeats(requested.size());
        booking.setPricePerSeat(pricePerSeat);
        booking.setTotalAmount(total);
        booking.setBookingOn(LocalDateTime.now());

        String rejection = null;
        if (event.getEventDate().isBefore(LocalDate.now())) {
            rejection = "Event date has passed";
        } else {
            Set<Integer> occupied = new HashSet<>();
            for (Booking confirmed : bookingRepository.findByEventAndStatus(eventId, BookingStatus.CONFIRMED)) {
                occupied.addAll(confirmed.getSeatNumber());
            }
            List<Integer> conflicts = requested.stream().filter(occupied::contains).distinct().toList();
            if (!conflicts.isEmpty()) {
                rejection = "Seat(s) already booked: " + conflicts;
            } else {
                long alreadyHeld = bookingRepository.countCustomerSeats(eventId, booking.getCustomerEmail(), BookingStatus.CONFIRMED);
                if (alreadyHeld + requested.size() > 6) {
                    rejection = "A customer may hold a maximum of 6 confirmed seats for this event";
                }
            }
        }

        if (rejection == null) {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setRejectionReason(null);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
            booking.setRejectionReason(rejection);
        }
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public Booking get(Integer id) {
        validateId(id);
        return bookingRepository.findWithEventById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking " + id + " was not found"));
    }

    @Transactional(readOnly = true)
    public List<Booking> listForEvent(Integer eventId, BookingStatus status) {
        if (eventId == null || eventId <= 0) throw new BadRequestException("id must be a positive integer");
        if (!eventRepository.existsById(eventId)) throw new BadRequestException("Event " + eventId + " does not exist");
        return status == null ? bookingRepository.findByEvent_IdOrderByBookingOnDesc(eventId)
                : bookingRepository.findByEvent_IdAndStatusOrderByBookingOnDesc(eventId, status);
    }

    @Transactional(readOnly = true)
    public List<Integer> availableSeats(Integer eventId) {
        Event event = eventOr404(eventId);
        Set<Integer> occupied = new HashSet<>();
        for (Booking booking : bookingRepository.findByEventAndStatus(eventId, BookingStatus.CONFIRMED)) {
            occupied.addAll(booking.getSeatNumber());
        }
        List<Integer> available = new ArrayList<>();
        for (int seat = 1; seat <= event.getTotalSeats(); seat++) {
            if (!occupied.contains(seat)) available.add(seat);
        }
        return available;
    }

    @Transactional(readOnly = true)
    public Event eventOr404(Integer eventId) {
        validateId(eventId);
        return eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event " + eventId + " was not found"));
    }

    public void validateId(Integer id) {
        if (id == null || id <= 0) throw new BadRequestException("id must be a positive integer");
    }

}