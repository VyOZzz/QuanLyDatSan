// file: src/main/java/com/codewithvy/quanlydatsan/controller/PriceRuleController.java
package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.dto.ApiResponse;
import com.codewithvy.quanlydatsan.dto.PriceRuleRequest;
import com.codewithvy.quanlydatsan.entity.PriceRules;
import com.codewithvy.quanlydatsan.entity.User;
import com.codewithvy.quanlydatsan.entity.Venues;
import com.codewithvy.quanlydatsan.repository.PriceRuleRepository;
import com.codewithvy.quanlydatsan.repository.UserRepository;
import com.codewithvy.quanlydatsan.repository.VenuesRepository;
import com.codewithvy.quanlydatsan.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricerules")
public class PriceRuleController {

    private final PriceRuleRepository priceRuleRepository;
    private final VenuesRepository venuesRepository;
    private final UserRepository userRepository;

    public PriceRuleController(PriceRuleRepository priceRuleRepository,
                              VenuesRepository venuesRepository,
                              UserRepository userRepository) {
        this.priceRuleRepository = priceRuleRepository;
        this.venuesRepository = venuesRepository;
        this.userRepository = userRepository;
    }

    /**
     * Tạo quy tắc giá mới cho venues (chỉ chủ sân)
     */
    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> createPriceRule(@RequestBody PriceRuleRequest request, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getVenueId() == null) {
            return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                    .success(false)
                    .message("venueId is required")
                    .build()
            );
        }

        Venues venue = venuesRepository.findById(request.getVenueId())
                .orElseThrow(() -> new RuntimeException("Venue not found"));

        // Kiểm tra xem user có phải là chủ sân không
        if (!venue.getOwner().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(
                ApiResponse.builder()
                    .success(false)
                    .message("You are not the owner of this venue")
                    .build()
            );
        }

        PriceRules priceRule = new PriceRules();
        priceRule.setName(request.getName());
        priceRule.setStartTime(request.getStartTime());
        priceRule.setEndTime(request.getEndTime());
        priceRule.setPricePerHour(request.getPricePerHour());
        priceRule.setVenues(venue);
        priceRule.setActive(true);

        PriceRules savedPriceRule = priceRuleRepository.save(priceRule);
        return ResponseEntity.ok(savedPriceRule);
    }

    /**
     * Lấy tất cả quy tắc giá của một venues
     */
    @GetMapping("/venue/{venueId}")
    public ResponseEntity<List<PriceRules>> getPriceRulesByVenue(@PathVariable Long venueId) {
        Venues venue = venuesRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));

        List<PriceRules> priceRules = priceRuleRepository.findByVenues(venue);
        return ResponseEntity.ok(priceRules);
    }

    /**
     * Cập nhật quy tắc giá (chỉ chủ sân)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> updatePriceRule(@PathVariable Long id,
                                            @RequestBody PriceRuleRequest request,
                                            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PriceRules priceRule = priceRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Price rule not found"));

        // Kiểm tra xem user có phải là chủ sân không
        if (!priceRule.getVenues().getOwner().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(
                ApiResponse.builder()
                    .success(false)
                    .message("You are not the owner of this venue")
                    .build()
            );
        }

        if (request.getName() != null) priceRule.setName(request.getName());
        if (request.getStartTime() != null) priceRule.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) priceRule.setEndTime(request.getEndTime());
        if (request.getPricePerHour() != null) priceRule.setPricePerHour(request.getPricePerHour());

        PriceRules updatedPriceRule = priceRuleRepository.save(priceRule);
        return ResponseEntity.ok(updatedPriceRule);
    }

    /**
     * Bật/tắt quy tắc giá (chỉ chủ sân)
     */
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> togglePriceRule(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PriceRules priceRule = priceRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Price rule not found"));

        // Kiểm tra xem user có phải là chủ sân không
        if (!priceRule.getVenues().getOwner().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(
                ApiResponse.builder()
                    .success(false)
                    .message("You are not the owner of this venue")
                    .build()
            );
        }

        priceRule.setActive(!priceRule.isActive());
        PriceRules updatedPriceRule = priceRuleRepository.save(priceRule);

        return ResponseEntity.ok(
            ApiResponse.builder()
                .success(true)
                .message("Price rule " + (updatedPriceRule.isActive() ? "activated" : "deactivated"))
                .data(updatedPriceRule)
                .build()
        );
    }

    /**
     * Xóa quy tắc giá (chỉ chủ sân)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> deletePriceRule(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PriceRules priceRule = priceRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Price rule not found"));

        // Kiểm tra xem user có phải là chủ sân không
        if (!priceRule.getVenues().getOwner().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(
                ApiResponse.builder()
                    .success(false)
                    .message("You are not the owner of this venue")
                    .build()
            );
        }

        priceRuleRepository.delete(priceRule);

        return ResponseEntity.ok(
            ApiResponse.builder()
                .success(true)
                .message("Price rule deleted successfully")
                .build()
        );
    }
}
