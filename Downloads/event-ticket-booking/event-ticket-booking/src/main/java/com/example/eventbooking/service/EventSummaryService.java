package com.example.eventbooking.service;

import com.example.eventbooking.dto.EventSummaryResponse;
import com.example.eventbooking.entity.BookingStatus;
import com.example.eventbooking.entity.Event;
import com.example.eventbooking.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class EventSummaryService {
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    public EventSummaryService(BookingRepository bookingRepository, BookingService bookingService) {
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
    }

    @Transactional(readOnly = true)
    public EventSummaryResponse getSummary(Integer eventId) {
        Event event = bookingService.eventOr404(eventId);
        int sold = bookingRepository.sumSeats(eventId, BookingStatus.CONFIRMED).intValue();
        BigDecimal revenue = bookingRepository.sumRevenue(eventId, BookingStatus.CONFIRMED)
                .setScale(2, RoundingMode.HALF_UP);
        int available = Math.max(0, event.getTotalSeats() - sold);
        BigDecimal occupancy = BigDecimal.valueOf(sold)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(event.getTotalSeats()), 1, RoundingMode.HALF_UP);
        String band = occupancy.compareTo(new BigDecimal("40.0")) < 0 ? "LOW"
                : occupancy.compareTo(new BigDecimal("75.0")) <= 0 ? "MEDIUM" : "HIGH";
        return new EventSummaryResponse(event.getId(), event.getName(), event.getTotalSeats(),
                sold, available, revenue, occupancy, band);
    }
}
