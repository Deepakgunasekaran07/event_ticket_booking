# Event Ticket Booking API

Spring Boot 3.4 / Java 21 / Spring Data JPA / Hibernate / PostgreSQL REST API.

## 1. Requirements
- JDK 21
- Maven 3.9+
- PostgreSQL

## 2. Create database
In psql or pgAdmin, run:
```sql
CREATE DATABASE event_booking;
```

Default connection settings are `localhost:5432`, database `event_booking`, username `postgres`, password `postgres`.
Change these with environment variables if needed:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `PORT` (default 8080)

## 3. Run
From this folder:
```bash
mvn spring-boot:run
```
Tables are created/updated by Hibernate for local development.

## 4. API endpoints
1. `POST /api/events` — create event; duplicate same name + date => 409.
2. `GET /api/events/{id}` — retrieve event.
3. `POST /api/bookings` — create booking attempt. Valid confirmed booking => 201; business rejection is saved and returned as 201 with `status: REJECTED`.
4. `GET /api/booking/{id}` — retrieve booking.
5. `GET /api/events/{id}/bookings?status=CONFIRMED|REJECTED` — list bookings; omitted status returns all.
6. `GET /api/events/{id}/seats` — available seats.
7. `GET /api/events/{id}/summary` — summary (`/summery` is also supported).

All request and response bodies are JSON. Invalid inputs return 400 with a list of field errors. API 5 returns 400 for a nonexistent event as explicitly requested.

## 5. Example event request
```json
{
  "name": "Music Fest",
  "venue": "Chennai Hall",
  "category": "Music",
  "event_date": "2026-12-20",
  "total_seats": 100,
  "base_price": 500.00
}
```

## 6. Example booking request
```json
{
  "event_id": 1,
  "customer_email": "deepak@example.com",
  "seat_numbers": [1, 2, 3, 4],
  "seats": 4
}
```

The per-seat price applies additive discounts (10% early bird + 5% for 4+ seats), rounded to two decimals. A customer's confirmed seats for an event are limited to six. Only confirmed bookings occupy seats. Business rejections such as occupied seats, passed event, or customer limit are saved with `REJECTED` and `rejection_reason`.

## Notes on concurrency
The booking service locks the event row during booking processing so simultaneous bookings for the same event are serialized. This prevents two concurrent requests from both passing the seat availability check before saving. For a high-volume production system, consider a normalized seat allocation table and database constraints.

## HTTP error conventions
- Event get, booking get, seats, summary: invalid/non-positive IDs => 400; missing resource => 404.
- Create event duplicate (same name and event date) => 409.
- Booking: missing event => 404; malformed/field-invalid request => 400.
- Booking business-rule rejection is a saved booking response (201 with `REJECTED`).
- API 5 missing event => 400 per requirement.
