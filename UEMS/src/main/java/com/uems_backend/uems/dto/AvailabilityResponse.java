package com.uems_backend.uems.dto;

import java.time.LocalDate;
import java.util.List;

public record AvailabilityResponse(
        Long venueId,
        String venueName,
        LocalDate date,
        List<TimeSlotResponse> availableSlots,
        List<EventResponse> bookings
) {
}
