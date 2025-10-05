package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.entity.Venues;
import com.codewithvy.quanlydatsan.repository.VenuesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/venues")
public class VenuesController {
    @Autowired
    private VenuesRepository venuesRepository;

    @GetMapping
    public List<Venues> getAllVenues() {
        return venuesRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venues> getVenuesById(@PathVariable Long id) {
        Optional<Venues> venues = venuesRepository.findById(id);
        return venues.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.<Venues>notFound().build());
    }

    @PostMapping
    public ResponseEntity<Venues> createVenues(@RequestBody Venues venues) {
        Venues saved = venuesRepository.save(venues);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Venues> updateVenues(@PathVariable Long id, @RequestBody Venues venuesDetails) {
        return venuesRepository.findById(id).map(venues -> {
            venues.setName(venuesDetails.getName());
            venues.setNumberOfCourt(venuesDetails.getNumberOfCourt());
            venues.setAddress(venuesDetails.getAddress());
            return ResponseEntity.ok(venuesRepository.save(venues));
        }).orElseGet(() -> ResponseEntity.<Venues>notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenues(@PathVariable Long id) {
        if (!venuesRepository.existsById(id)) {
            return ResponseEntity.<Void>notFound().build();
        }
        venuesRepository.deleteById(id);
        return ResponseEntity.<Void>noContent().build();
    }
}
