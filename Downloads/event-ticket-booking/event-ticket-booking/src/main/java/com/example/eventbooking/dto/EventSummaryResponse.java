package com.example.eventbooking.dto;

import java.math.BigDecimal;

public record EventSummaryResponse(Integer event_id, String name, Integer total_seats,
                                   Integer seats_sold, Integer seats_available,
                                   BigDecimal total_revenue, BigDecimal occupancy_pct,
                                   String demand_band) {}
