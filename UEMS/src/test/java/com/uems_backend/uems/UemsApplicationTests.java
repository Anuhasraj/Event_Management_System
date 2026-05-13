package com.uems_backend.uems;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uems_backend.uems.dto.EventRequest;
import com.uems_backend.uems.dto.LoginRequest;
import com.uems_backend.uems.dto.RegisterRequest;
import com.uems_backend.uems.dto.AvailabilityResponse;
import com.uems_backend.uems.exception.BadRequestException;
import com.uems_backend.uems.model.AppUser;
import com.uems_backend.uems.model.Event;
import com.uems_backend.uems.model.EventStatus;
import com.uems_backend.uems.model.Role;
import com.uems_backend.uems.model.Venue;
import com.uems_backend.uems.repository.UserRepository;
import com.uems_backend.uems.repository.VenueRepository;
import com.uems_backend.uems.service.AuthService;
import com.uems_backend.uems.service.EventService;
import com.uems_backend.uems.service.UserService;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UemsApplicationTests {
    @Autowired
    private AuthService authService;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private UserService userService;

    @Test
    void contextLoads() {
    }

    @Test
    void savesNewOrganizer() {
        authService.register(new RegisterRequest("organizer1", "organizer1@test.com", "secret123", Role.ORGANIZER));

        AppUser organizer = userRepository.findByUsername("organizer1").orElseThrow();

        assertThat(organizer.getRole()).isEqualTo(Role.ORGANIZER);
        assertThat(organizer.getEmail()).isEqualTo("organizer1@test.com");
    }

    @Test
    void publicRegistrationRejectsAdminRole() {
        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("badadmin", "badadmin@test.com", "secret123", Role.ADMIN)
        )).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("only available for organizers");
    }

    @Test
    void disabledUserCannotLogin() {
        authService.register(new RegisterRequest("disabled1", "disabled1@test.com", "secret123", Role.ORGANIZER));
        AppUser user = userRepository.findByUsername("disabled1").orElseThrow();

        userService.setEnabled(user.getId(), false, "admin");

        assertThatThrownBy(() -> authService.login(new LoginRequest("disabled1", "secret123")))
                .hasMessageContaining("Account is disabled");
    }

    @Test
    void eventRequestStartsAsPending() {
        authService.register(new RegisterRequest("organizer2", "organizer2@test.com", "secret123", Role.ORGANIZER));
        Venue auditorium = venueRepository.findByName("Auditorium").orElseThrow();

        Event event = eventService.requestEvent(new EventRequest(
                "Tech Talk",
                LocalDate.of(2026, 6, 10),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                auditorium.getId(),
                "Monthly technology session"
        ), "organizer2");

        assertThat(event.getStatus()).isEqualTo(EventStatus.PENDING);
    }

    @Test
    void rejectsOverlappingEventForSameVenueAndTime() {
        authService.register(new RegisterRequest("organizer3", "organizer3@test.com", "secret123", Role.ORGANIZER));
        authService.register(new RegisterRequest("organizer4", "organizer4@test.com", "secret123", Role.ORGANIZER));
        Venue auditorium = venueRepository.findByName("Auditorium").orElseThrow();

        eventService.requestEvent(new EventRequest(
                "Morning Seminar",
                LocalDate.of(2026, 7, 5),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                auditorium.getId(),
                "First event"
        ), "organizer3");

        assertThatThrownBy(() -> eventService.requestEvent(new EventRequest(
                "Overlapping Workshop",
                LocalDate.of(2026, 7, 5),
                LocalTime.of(11, 0),
                LocalTime.of(13, 0),
                auditorium.getId(),
                "Second event"
        ), "organizer4")).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    void availabilityReturnsFreeSlotsAroundBookings() {
        authService.register(new RegisterRequest("organizer5", "organizer5@test.com", "secret123", Role.ORGANIZER));
        Venue auditorium = venueRepository.findByName("Auditorium").orElseThrow();

        eventService.requestEvent(new EventRequest(
                "Booked Session",
                LocalDate.of(2026, 8, 15),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                auditorium.getId(),
                "Reserved block"
        ), "organizer5");

        AvailabilityResponse availability = eventService.findAvailability(auditorium.getId(), LocalDate.of(2026, 8, 15));

        assertThat(availability.availableSlots()).extracting("startTime", "endTime")
                .contains(
                        org.assertj.core.groups.Tuple.tuple(LocalTime.of(8, 0), LocalTime.of(9, 0)),
                        org.assertj.core.groups.Tuple.tuple(LocalTime.of(11, 0), LocalTime.of(18, 0))
                );
        assertThat(availability.bookings()).hasSize(1);
    }
}
