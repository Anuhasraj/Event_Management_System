package com.uems_backend.uems.service;

import com.uems_backend.uems.dto.VenueRequest;
import com.uems_backend.uems.exception.BadRequestException;
import com.uems_backend.uems.exception.NotFoundException;
import com.uems_backend.uems.model.Venue;
import com.uems_backend.uems.repository.EventRepository;
import com.uems_backend.uems.repository.VenueRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VenueService {
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;

    public VenueService(VenueRepository venueRepository, EventRepository eventRepository) {
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<Venue> findAll() {
        return venueRepository.findAll();
    }

    @Transactional
    public Venue create(VenueRequest request) {
        String name = validateName(request);
        if (venueRepository.existsByName(name)) {
            throw new BadRequestException("Venue already exists");
        }
        return venueRepository.save(new Venue(name));
    }

    @Transactional
    public Venue update(Long id, VenueRequest request) {
        Venue venue = findVenue(id);
        String name = validateName(request);
        if (venueRepository.existsByNameAndIdNot(name, id)) {
            throw new BadRequestException("Venue already exists");
        }
        venue.rename(name);
        return venue;
    }

    @Transactional
    public void delete(Long id) {
        Venue venue = findVenue(id);
        if (eventRepository.existsByVenueId(id)) {
            throw new BadRequestException("Venues with events cannot be deleted");
        }
        venueRepository.delete(venue);
    }

    private Venue findVenue(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venue not found"));
    }

    private String validateName(VenueRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Venue name is required");
        }
        return request.name().trim();
    }
}
