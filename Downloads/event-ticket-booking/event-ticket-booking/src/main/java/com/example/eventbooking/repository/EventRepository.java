package com.example.eventbooking.repository;

import com.example.eventbooking.entity.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {
    boolean existsByNameIgnoreCaseAndEventDate(String name, java.time.LocalDate eventDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Integer id);
}
