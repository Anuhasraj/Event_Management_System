package com.uems_backend.uems.controller;

import com.uems_backend.uems.dto.VenueRequest;
import com.uems_backend.uems.dto.VenueResponse;
import com.uems_backend.uems.service.VenueService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/venues")
public class VenueController {
    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping
    public List<VenueResponse> allVenues() {
        return venueService.findAll().stream()
                .map(VenueResponse::from)
                .toList();
    }

    @PostMapping
    public VenueResponse create(@RequestBody VenueRequest request) {
        return VenueResponse.from(venueService.create(request));
    }

    @PutMapping("/{id}")
    public VenueResponse update(@PathVariable Long id, @RequestBody VenueRequest request) {
        return VenueResponse.from(venueService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        venueService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
