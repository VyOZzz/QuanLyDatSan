package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.entity.Court;
import com.codewithvy.quanlydatsan.entity.Venues;
import com.codewithvy.quanlydatsan.repository.CourtRepository;
import com.codewithvy.quanlydatsan.repository.VenuesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courts")
public class CourtController {
    @Autowired
    private CourtRepository courtRepository;
    @Autowired
    private VenuesRepository venuesRepository;

    @GetMapping
    public List<Court> getAllCourts() {
        return courtRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Court> getCourtById(@PathVariable Long id) {
        Optional<Court> court = courtRepository.findById(id);
        return court.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.<Court>notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCourt(@RequestBody Court court) {
        if (court.getVenues() == null || court.getVenues().getId() == null) {
            return ResponseEntity.badRequest().body("venues.id is required");
        }
        Optional<Venues> venues = venuesRepository.findById(court.getVenues().getId());
        if (venues.isEmpty()) {
            return ResponseEntity.badRequest().body("Venues not found");
        }
        court.setVenues(venues.get());
        return ResponseEntity.ok(courtRepository.save(court));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Court> updateCourt(@PathVariable Long id, @RequestBody Court courtDetails) {
        return courtRepository.findById(id).map(court -> {
            court.setBooked(courtDetails.isBooked());
            if (courtDetails.getVenues() != null && courtDetails.getVenues().getId() != null) {
                venuesRepository.findById(courtDetails.getVenues().getId()).ifPresent(court::setVenues);
            }
            return ResponseEntity.ok(courtRepository.save(court));
        }).orElseGet(() -> ResponseEntity.<Court>notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourt(@PathVariable Long id) {
        if (!courtRepository.existsById(id)) {
            return ResponseEntity.<Void>notFound().build();
        }
        courtRepository.deleteById(id);
        return ResponseEntity.<Void>noContent().build();
    }
}
