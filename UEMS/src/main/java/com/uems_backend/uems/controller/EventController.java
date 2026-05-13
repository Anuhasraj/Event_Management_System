package com.uems_backend.uems.controller;

import com.uems_backend.uems.dto.AvailabilityResponse;
import com.uems_backend.uems.dto.EventRequest;
import com.uems_backend.uems.dto.EventResponse;
import com.uems_backend.uems.service.EventService;
import java.security.Principal;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/request")
    public EventResponse requestEvent(@RequestBody EventRequest request, Principal principal) {
        return EventResponse.from(eventService.requestEvent(request, principal.getName()));
    }

    @GetMapping("/my")
    public List<EventResponse> myEvents(Principal principal) {
        return eventService.findMyEvents(principal.getName()).stream()
                .map(EventResponse::from)
                .toList();
    }

    @GetMapping("/venue/{venueId}/schedule")
    public List<EventResponse> venueSchedule(@PathVariable Long venueId,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return eventService.findVenueSchedule(venueId, date).stream()
                .map(EventResponse::from)
                .toList();
    }

    @GetMapping("/venue/{venueId}/availability")
    public AvailabilityResponse venueAvailability(@PathVariable Long venueId,
                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return eventService.findAvailability(venueId, date);
    }

    @GetMapping("/admin/pending")
    public List<EventResponse> pendingEvents() {
        return eventService.findPendingEvents().stream()
                .map(EventResponse::from)
                .toList();
    }

    @GetMapping("/admin/all")
    public List<EventResponse> allEvents() {
        return eventService.findAllEvents().stream()
                .map(EventResponse::from)
                .toList();
    }

    @PutMapping("/admin/approve/{eventId}")
    public EventResponse approve(@PathVariable Long eventId) {
        return EventResponse.from(eventService.approve(eventId));
    }

    @PutMapping("/admin/reject/{eventId}")
    public EventResponse reject(@PathVariable Long eventId) {
        return EventResponse.from(eventService.reject(eventId));
    }

    @PutMapping("/admin/{eventId}")
    public EventResponse updateByAdmin(@PathVariable Long eventId, @RequestBody EventRequest request) {
        return EventResponse.from(eventService.adminUpdateEvent(eventId, request));
    }
}
