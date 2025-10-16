// file: src/main/java/com/codewithvy/quanlydatsan/controller/PriceRuleController.java
package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.entity.PriceRules;
import com.codewithvy.quanlydatsan.entity.Venues;
import com.codewithvy.quanlydatsan.repository.PriceRuleRepository;
import com.codewithvy.quanlydatsan.repository.VenuesRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricerules")
public class PriceRuleController {

    private final PriceRuleRepository priceRuleRepository;
    private final VenuesRepository venuesRepository;

    public PriceRuleController(PriceRuleRepository priceRuleRepository, VenuesRepository venuesRepository) {
        this.priceRuleRepository = priceRuleRepository;
        this.venuesRepository = venuesRepository;
    }

    // Endpoint để admin thêm một quy tắc giá mới cho venue
    @PostMapping("/venue/{venueId}")
    public ResponseEntity<PriceRules> createPriceRule(@PathVariable Long venueId, @RequestBody PriceRules priceRuleRequest) {
        Venues venue = venuesRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found with id: " + venueId));
        priceRuleRequest.setVenues(venue);
        PriceRules savedPriceRule = priceRuleRepository.save(priceRuleRequest);
        return ResponseEntity.ok(savedPriceRule);
    }
}
