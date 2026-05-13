package com.uems_backend.uems.repository;

import com.uems_backend.uems.model.Event;
import com.uems_backend.uems.model.EventStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizerUsernameOrderByEventDateDescStartTimeDesc(String username);

    List<Event> findByStatusOrderByEventDateAscStartTimeAsc(EventStatus status);

    List<Event> findByVenueIdAndEventDateOrderByStartTimeAsc(Long venueId, LocalDate eventDate);

    boolean existsByOrganizerId(Long organizerId);

    boolean existsByVenueId(Long venueId);

    @Query("""
            select count(e) > 0 from Event e
            where e.venue.id = :venueId
              and e.eventDate = :eventDate
              and e.status in :statuses
              and e.startTime < :endTime
              and e.endTime > :startTime
            """)
    boolean existsOverlappingEvent(@Param("venueId") Long venueId,
                                   @Param("eventDate") LocalDate eventDate,
                                   @Param("startTime") LocalTime startTime,
                                   @Param("endTime") LocalTime endTime,
                                   @Param("statuses") Collection<EventStatus> statuses);

    @Query("""
            select count(e) > 0 from Event e
            where e.eventId <> :eventId
              and e.venue.id = :venueId
              and e.eventDate = :eventDate
              and e.status in :statuses
              and e.startTime < :endTime
              and e.endTime > :startTime
            """)
    boolean existsOverlappingEventExcluding(@Param("eventId") Long eventId,
                                            @Param("venueId") Long venueId,
                                            @Param("eventDate") LocalDate eventDate,
                                            @Param("startTime") LocalTime startTime,
                                            @Param("endTime") LocalTime endTime,
                                            @Param("statuses") Collection<EventStatus> statuses);
}
