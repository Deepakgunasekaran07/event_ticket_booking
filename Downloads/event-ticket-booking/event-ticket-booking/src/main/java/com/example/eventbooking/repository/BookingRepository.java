package com.example.eventbooking.repository;

import com.example.eventbooking.entity.Booking;
import com.example.eventbooking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByEvent_IdOrderByBookingOnDesc(Integer eventId);
    List<Booking> findByEvent_IdAndStatusOrderByBookingOnDesc(Integer eventId, BookingStatus status);

    @Query("select b from Booking b join fetch b.event where b.id = :id")
    java.util.Optional<Booking> findWithEventById(@Param("id") Integer id);

    @Query("select b from Booking b where b.event.id = :eventId and b.status = :status")
    List<Booking> findByEventAndStatus(@Param("eventId") Integer eventId, @Param("status") BookingStatus status);

    @Query("select coalesce(sum(b.seats), 0) from Booking b where b.event.id = :eventId and b.customerEmail = :email and b.status = :status")
    Long countCustomerSeats(@Param("eventId") Integer eventId, @Param("email") String email, @Param("status") BookingStatus status);

    @Query("select coalesce(sum(b.totalAmount), 0) from Booking b where b.event.id = :eventId and b.status = :status")
    java.math.BigDecimal sumRevenue(@Param("eventId") Integer eventId, @Param("status") BookingStatus status);

    @Query("select coalesce(sum(b.seats), 0) from Booking b where b.event.id = :eventId and b.status = :status")
    Long sumSeats(@Param("eventId") Integer eventId, @Param("status") BookingStatus status);
}
