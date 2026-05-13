package com.uems_backend.uems.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventRequest(
        String eventName,
        LocalDate eventDate,
        LocalTime startTime,
        LocalTime endTime,
        Long venueId,
        String description
) {
}
