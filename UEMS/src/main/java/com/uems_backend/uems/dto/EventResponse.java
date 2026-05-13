package com.uems_backend.uems.dto;

import com.uems_backend.uems.model.Event;
import com.uems_backend.uems.model.EventStatus;
import java.time.LocalDate;
import java.time.LocalTime;

public record EventResponse(
        Long eventId,
        String eventName,
        LocalDate eventDate,
        LocalTime startTime,
        LocalTime endTime,
        String description,
        Long venueId,
        String venueName,
        Long organizerId,
        String organizerUsername,
        EventStatus status
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getEventId(),
                event.getEventName(),
                event.getEventDate(),
                event.getStartTime(),
                event.getEndTime(),
                event.getDescription(),
                event.getVenue().getId(),
                event.getVenue().getName(),
                event.getOrganizer().getId(),
                event.getOrganizer().getUsername(),
                event.getStatus()
        );
    }
}
