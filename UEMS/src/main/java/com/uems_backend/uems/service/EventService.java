package com.uems_backend.uems.service;

import com.uems_backend.uems.dto.AvailabilityResponse;
import com.uems_backend.uems.dto.EventRequest;
import com.uems_backend.uems.dto.EventResponse;
import com.uems_backend.uems.dto.TimeSlotResponse;
import com.uems_backend.uems.exception.BadRequestException;
import com.uems_backend.uems.exception.NotFoundException;
import com.uems_backend.uems.model.AppUser;
import com.uems_backend.uems.model.Event;
import com.uems_backend.uems.model.EventStatus;
import com.uems_backend.uems.model.Role;
import com.uems_backend.uems.model.Venue;
import com.uems_backend.uems.notification.EventStatusObserver;
import com.uems_backend.uems.repository.EventRepository;
import com.uems_backend.uems.repository.UserRepository;
import com.uems_backend.uems.repository.VenueRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {
    private static final List<EventStatus> BLOCKING_STATUSES = List.of(EventStatus.PENDING, EventStatus.APPROVED);
    private static final LocalTime DAY_START = LocalTime.of(8, 0);
    private static final LocalTime DAY_END = LocalTime.of(18, 0);

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final List<EventStatusObserver> statusObservers;

    public EventService(EventRepository eventRepository, VenueRepository venueRepository, UserRepository userRepository,
                        List<EventStatusObserver> statusObservers) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.userRepository = userRepository;
        this.statusObservers = statusObservers;
    }

    @Transactional
    public Event requestEvent(EventRequest request, String organizerUsername) {
        AppUser organizer = userRepository.findByUsername(organizerUsername)
                .orElseThrow(() -> new NotFoundException("Organizer not found"));
        if (organizer.getRole() != Role.ORGANIZER) {
            throw new BadRequestException("Only organizers can request events");
        }
        validateRequest(request);

        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new NotFoundException("Venue not found"));
        boolean hasOverlap = eventRepository.existsOverlappingEvent(
                request.venueId(),
                request.eventDate(),
                request.startTime(),
                request.endTime(),
                BLOCKING_STATUSES
        );
        if (hasOverlap) {
            throw new BadRequestException("Venue is already booked for the selected date and time");
        }

        return eventRepository.save(new Event(
                request.eventName(),
                request.eventDate(),
                request.startTime(),
                request.endTime(),
                request.description(),
                venue,
                organizer
        ));
    }

    @Transactional(readOnly = true)
    public List<Event> findMyEvents(String username) {
        return eventRepository.findByOrganizerUsernameOrderByEventDateDescStartTimeDesc(username);
    }

    @Transactional(readOnly = true)
    public List<Event> findPendingEvents() {
        return eventRepository.findByStatusOrderByEventDateAscStartTimeAsc(EventStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Event> findAllEvents() {
        return eventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Event> findVenueSchedule(Long venueId, LocalDate date) {
        return eventRepository.findByVenueIdAndEventDateOrderByStartTimeAsc(venueId, date);
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse findAvailability(Long venueId, LocalDate date) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new NotFoundException("Venue not found"));
        List<Event> bookings = eventRepository.findByVenueIdAndEventDateOrderByStartTimeAsc(venueId, date)
                .stream()
                .filter(event -> BLOCKING_STATUSES.contains(event.getStatus()))
                .toList();

        List<TimeSlotResponse> availableSlots = new ArrayList<>();
        LocalTime cursor = DAY_START;
        for (Event booking : bookings) {
            if (booking.getStartTime().isAfter(cursor)) {
                availableSlots.add(new TimeSlotResponse(cursor, booking.getStartTime()));
            }
            if (booking.getEndTime().isAfter(cursor)) {
                cursor = booking.getEndTime();
            }
        }
        if (cursor.isBefore(DAY_END)) {
            availableSlots.add(new TimeSlotResponse(cursor, DAY_END));
        }

        return new AvailabilityResponse(
                venue.getId(),
                venue.getName(),
                date,
                availableSlots,
                bookings.stream().map(EventResponse::from).toList()
        );
    }

    @Transactional
    public Event approve(Long eventId) {
        Event event = findEvent(eventId);
        event.approve();
        notifyStatusChanged(event);
        return event;
    }

    @Transactional
    public Event reject(Long eventId) {
        Event event = findEvent(eventId);
        event.reject();
        notifyStatusChanged(event);
        return event;
    }

    @Transactional
    public Event adminUpdateEvent(Long eventId, EventRequest request) {
        validateRequest(request);
        Event event = findEvent(eventId);
        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new NotFoundException("Venue not found"));

        boolean hasOverlap = eventRepository.existsOverlappingEventExcluding(
                eventId,
                request.venueId(),
                request.eventDate(),
                request.startTime(),
                request.endTime(),
                BLOCKING_STATUSES
        );
        if (hasOverlap) {
            throw new BadRequestException("Venue is already booked for the selected date and time");
        }

        event.updateDetails(
                request.eventName(),
                request.eventDate(),
                request.startTime(),
                request.endTime(),
                request.description(),
                venue
        );
        return event;
    }

    private Event findEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
    }

    private void notifyStatusChanged(Event event) {
        statusObservers.forEach(observer -> observer.onStatusChanged(event));
    }

    private void validateRequest(EventRequest request) {
        if (request.eventName() == null || request.eventName().isBlank()) {
            throw new BadRequestException("Event name is required");
        }
        if (request.eventDate() == null) {
            throw new BadRequestException("Event date is required");
        }
        if (request.startTime() == null || request.endTime() == null) {
            throw new BadRequestException("Start time and end time are required");
        }
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("End time must be after start time");
        }
        if (request.venueId() == null) {
            throw new BadRequestException("Venue is required");
        }
    }
}
