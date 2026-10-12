package com.example.eventbooking.service;

import com.example.eventbooking.dto.EventRequest;
import com.example.eventbooking.entity.Event;
import com.example.eventbooking.exception.BadRequestException;
import com.example.eventbooking.exception.DuplicateEventException;
import com.example.eventbooking.exception.ResourceNotFoundException;
import com.example.eventbooking.repository.EventRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) { this.eventRepository = eventRepository; }

    @Transactional
    public Event create(EventRequest request) {
        if (eventRepository.existsByNameIgnoreCaseAndEventDate(request.name().trim(), request.event_date())) {
            throw new DuplicateEventException("An event with the same name and event date already exists");
        }
        Event event = new Event();
        event.setName(request.name().trim());
        event.setVenue(request.venue().trim());
        event.setCategory(request.category().trim());
        event.setEventDate(request.event_date());
        event.setTotalSeats(request.total_seats());
        event.setBasePrice(request.base_price().setScale(2, java.math.RoundingMode.HALF_UP));
        try {
            return eventRepository.saveAndFlush(event);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateEventException("An event with the same name and event date already exists");
        }
    }

    @Transactional(readOnly = true)
    public Event get(Integer id) {
        validateId(id);
        return eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event " + id + " was not found"));
    }

    public void validateId(Integer id) {
        if (id == null || id <= 0) throw new BadRequestException("id must be a positive integer");
    }
}
