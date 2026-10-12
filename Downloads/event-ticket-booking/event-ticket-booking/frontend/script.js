
const API_URL = "http://localhost:8080/api";

let currentEvent = null;
let availableSeatNumbers = [];
let availableSeatsLoaded = false;

function showSection(id) {
    document.querySelectorAll(".section").forEach(section => {
        section.classList.remove("active");
    });

    document.getElementById(id).classList.add("active");
    showMessage("", "");
}

function showMessage(message, type = "success") {
    const box = document.getElementById("message");
    box.textContent = message;
    box.className = message ? type : "";
}

function display(elementId, data, type = "info") {
    const element = document.getElementById(elementId);
    element.replaceChildren();

    if (data == null) return;

    element.className = type;

    const pre = document.createElement("pre");
    pre.style.whiteSpace = "pre-wrap";
    pre.style.overflowWrap = "anywhere";
    pre.textContent = typeof data === "string"
        ? data
        : JSON.stringify(data, null, 2);

    element.appendChild(pre);
}

async function apiRequest(path, options = {}) {
    let response;

    try {
        response = await fetch(API_URL + path, {
            ...options,
            headers: {
                "Content-Type": "application/json",
                ...options.headers
            }
        });
    } catch {
        throw new Error(
            "Cannot connect to Spring Boot. Check that the backend is running on port 8080."
        );
    }

    const text = await response.text();
    let data = null;

    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = text;
        }
    }

    if (!response.ok) {
        const fields = data?.field_errors
            ?.map(error => `${error.field}: ${error.message}`)
            .join("\n");

        throw new Error(
            fields ||
            data?.message ||
            (typeof data === "string" ? data : null) ||
            `HTTP error ${response.status}`
        );
    }

    return data;
}

function getEventName(event) {
    return event.name || "Event";
}

function showEvent(event) {
    currentEvent = event;

    display("eventDetails", {
        id: event.id,
        name: event.name,
        category: event.category,
        venue: event.venue,
        event_date: event.event_date,
        total_seats: event.total_seats,
        base_price: event.base_price
    });

    document.getElementById("bookingEventId").value = event.id;
    document.getElementById("summaryEventId").value = event.id;
}

// Retrieve one event using the existing GET /api/events/{id}.
document.getElementById("findEventForm")
    .addEventListener("submit", async event => {
        event.preventDefault();

        const id = document.getElementById("findEventId").value;

        try {
            const result = await apiRequest(`/events/${id}`);
            showEvent(result);
            showMessage(`Event found: ${getEventName(result)}`);
        } catch (error) {
            showMessage(error.message, "error");
        }
    });

// Create event using POST /api/events.
document.getElementById("eventForm")
    .addEventListener("submit", async event => {
        event.preventDefault();

        const payload = {
            name: document.getElementById("name").value.trim(),
            venue: document.getElementById("venue").value.trim(),
            category: document.getElementById("category").value.trim(),
            event_date: document.getElementById("eventDate").value,
            total_seats: Number(
                document.getElementById("totalSeats").value
            ),
            base_price: Number(
                document.getElementById("basePrice").value
            )
        };

        if (payload.event_date < new Date().toLocaleDateString("en-CA")) {
            showMessage("Choose today or a future event date.", "error");
            return;
        }

        try {
            const result = await apiRequest("/events", {
                method: "POST",
                body: JSON.stringify(payload)
            });

            display("createdEvent", result);
            showEvent(result);
            document.getElementById("findEventId").value = result.id;
            document.getElementById("eventForm").reset();

            showMessage(`Event created successfully. Event ID: ${result.id}`);
        } catch (error) {
            showMessage(error.message, "error");
        }
    });

// Load available seats using GET /api/events/{id}/seats.
document.getElementById("loadSeats")
    .addEventListener("click", async () => {
        const id = document.getElementById("bookingEventId").value;

        if (!id) {
            showMessage("Enter an event ID first.", "error");
            return;
        }

        try {
            const result = await apiRequest(`/events/${id}/seats`);

            availableSeatNumbers = result.available_seats || [];
            availableSeatsLoaded = true;

            display("seatInfo", {
                event_id: result.event_id,
                total_seats: result.total_seats,
                seats_available: result.seats_available,
                available_seats: availableSeatNumbers
            });

            updatePrice();
            showMessage("Available seats loaded.");
        } catch (error) {
            availableSeatsLoaded = false;
            showMessage(error.message, "error");
        }
    });

function parseSeatNumbers() {
    const value = document.getElementById("seatNumbers").value.trim();

    if (!value) return [];

    return value.split(",").map(seat => Number(seat.trim()));
}

function updatePrice() {
    const seats = parseSeatNumbers();
    const output = document.getElementById("priceInfo");

    if (!seats.length || seats.some(seat =>
        !Number.isInteger(seat) || seat <= 0)) {
        output.textContent = "Enter valid seat numbers separated by commas.";
        return;
    }

    if (new Set(seats).size !== seats.length) {
        output.textContent = "Duplicate seat numbers are not allowed.";
        return;
    }

    if (seats.length > 6) {
        output.textContent = "You can request a maximum of 6 seats.";
        return;
    }

    if (availableSeatsLoaded &&
        seats.some(seat => !availableSeatNumbers.includes(seat))) {
        output.textContent = "One or more seats are not available. Reload seats.";
        return;
    }

    if (currentEvent &&
        String(currentEvent.id) ===
        document.getElementById("bookingEventId").value) {

        const base = Number(currentEvent.base_price);
        let discount = 0;

        const eventDate = new Date(currentEvent.event_date + "T00:00:00");
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        const days = (eventDate - today) / (1000 * 60 * 60 * 24);

        if (days >= 30) discount += 0.10;
        if (seats.length >= 4) discount += 0.05;

        const perSeat = Math.round(base * (1 - discount) * 100) / 100;
        const total = Math.round(perSeat * seats.length * 100) / 100;

        output.textContent =
            `Estimated price per seat: ₹${perSeat.toFixed(2)} | ` +
            `Estimated total: ₹${total.toFixed(2)}. ` +
            "The backend determines the final booking price.";
    } else {
        output.textContent =
            `${seats.length} seat(s) selected. Load the event details to estimate price.`;
    }
}

document.getElementById("seatNumbers")
    .addEventListener("input", updatePrice);

document.getElementById("bookingEventId")
    .addEventListener("input", () => {
        availableSeatsLoaded = false;
        currentEvent = null;
        document.getElementById("seatInfo").replaceChildren();
        updatePrice();
    });

// Submit booking using the exact BookingRequest DTO fields.
document.getElementById("bookingForm")
    .addEventListener("submit", async event => {
        event.preventDefault();

        const eventId = Number(
            document.getElementById("bookingEventId").value
        );
        const email = document.getElementById("email").value.trim();
        const seats = parseSeatNumbers();

        if (!Number.isInteger(eventId) || eventId <= 0) {
            showMessage("Enter a valid event ID.", "error");
            return;
        }

        if (!seats.length || seats.length > 6 ||
            seats.some(seat => !Number.isInteger(seat) || seat <= 0)) {
            showMessage("Enter between 1 and 6 valid seat numbers.", "error");
            return;
        }

        if (new Set(seats).size !== seats.length) {
            showMessage("Duplicate seat numbers are not allowed.", "error");
            return;
        }

        if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            showMessage("Enter a valid customer email.", "error");
            return;
        }

        if (availableSeatsLoaded &&
            seats.some(seat => !availableSeatNumbers.includes(seat))) {
            showMessage("Some selected seats are unavailable. Reload seats.", "error");
            return;
        }

        const payload = {
            event_id: eventId,
            customer_email: email,
            seat_numbers: seats,
            seats: seats.length
        };

        try {
            const result = await apiRequest("/bookings", {
                method: "POST",
                body: JSON.stringify(payload)
            });

            display("bookingResult", result,
                result.status === "CONFIRMED" ? "success" : "error");

            if (result.status === "CONFIRMED") {
                showMessage(`Booking confirmed! Booking ID: ${result.id}`);
            } else {
                showMessage(
                    result.rejection_reason || "Booking was rejected.",
                    "error"
                );
            }

            availableSeatsLoaded = false;
        } catch (error) {
            showMessage(error.message, "error");
        }
    });

// Retrieve event summary using GET /api/events/{id}/summary.
document.getElementById("summaryForm")
    .addEventListener("submit", async event => {
        event.preventDefault();

        const id = document.getElementById("summaryEventId").value;

        try {
            const result = await apiRequest(`/events/${id}/summary`);
            display("summaryResult", result);
            showMessage("Event summary loaded.");
        } catch (error) {
            showMessage(error.message, "error");
        }
    });
