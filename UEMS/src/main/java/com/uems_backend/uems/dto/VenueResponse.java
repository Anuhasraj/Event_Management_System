package com.uems_backend.uems.dto;

import com.uems_backend.uems.model.Venue;

public record VenueResponse(Long id, String name) {
    public static VenueResponse from(Venue venue) {
        return new VenueResponse(venue.getId(), venue.getName());
    }
}
