package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.dto.CourtRequest;
import com.codewithvy.quanlydatsan.entity.BookingItem;
import com.codewithvy.quanlydatsan.entity.Court;
import com.codewithvy.quanlydatsan.entity.Venues;
import com.codewithvy.quanlydatsan.repository.BookingItemRepository;
import com.codewithvy.quanlydatsan.repository.CourtRepository;
import com.codewithvy.quanlydatsan.repository.VenuesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courts")
public class CourtController {
    @Autowired
    private CourtRepository courtRepository;
    @Autowired
    private VenuesRepository venuesRepository;
    @Autowired
    private BookingItemRepository bookingItemRepository;

    @GetMapping
    public List<Court> getAllCourts() {
        return courtRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Court> getCourtById(@PathVariable Long id) {
        Optional<Court> court = courtRepository.findById(id);
        return court.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.<Court>notFound().build());
    }

    /**
     * API mới: Kiểm tra lịch trống của sân trong khoảng thời gian
     * GET /api/courts/{id}/availability?startTime=...&endTime=...
     */
    @GetMapping("/{id}/availability")
    public ResponseEntity<?> checkAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        Optional<Court> courtOpt = courtRepository.findById(id);
        if (courtOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Lấy danh sách các slot đã đặt trong khoảng thời gian
        List<BookingItem> bookedSlots = bookingItemRepository.findBookedSlots(id, startTime, endTime);

        Map<String, Object> response = new HashMap<>();
        response.put("courtId", id);
        response.put("available", bookedSlots.isEmpty());
        response.put("bookedSlots", bookedSlots.stream().map(slot -> {
            Map<String, Object> slotInfo = new HashMap<>();
            slotInfo.put("startTime", slot.getStartTime());
            slotInfo.put("endTime", slot.getEndTime());
            slotInfo.put("bookingId", slot.getBooking().getId());
            return slotInfo;
        }).collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createCourt(@RequestBody CourtRequest request) {
        if (request.getVenueId() == null) {
            return ResponseEntity.badRequest().body("venueId is required");
        }

        Optional<Venues> venuesOpt = venuesRepository.findById(request.getVenueId());
        if (venuesOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Venues not found");
        }

        Venues venues = venuesOpt.get();

        // Tạo Court mới từ request
        Court court = new Court();
        court.setDescription(request.getDescription());
        // Xóa setBooked - không cần nữa
        court.setVenues(venues);

        Court savedCourt = courtRepository.save(court);

        // Tự động tăng numberOfCourt
        venues.setNumberOfCourt(venues.getNumberOfCourt() + 1);
        venuesRepository.save(venues);

        return ResponseEntity.ok(savedCourt);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Court> updateCourt(@PathVariable Long id, @RequestBody Court courtDetails) {
        return courtRepository.findById(id).map(court -> {
            // Xóa setBooked - không cần nữa
            court.setDescription(courtDetails.getDescription());
            if (courtDetails.getVenues() != null && courtDetails.getVenues().getId() != null) {
                venuesRepository.findById(courtDetails.getVenues().getId()).ifPresent(court::setVenues);
            }
            return ResponseEntity.ok(courtRepository.save(court));
        }).orElseGet(() -> ResponseEntity.<Court>notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourt(@PathVariable Long id) {
        Optional<Court> courtOpt = courtRepository.findById(id);
        if (courtOpt.isEmpty()) {
            return ResponseEntity.<Void>notFound().build();
        }

        Court court = courtOpt.get();
        Venues venues = court.getVenues();

        // Xóa court
        courtRepository.deleteById(id);

        // Tự động giảm numberOfCourt
        if (venues != null && venues.getNumberOfCourt() > 0) {
            venues.setNumberOfCourt(venues.getNumberOfCourt() - 1);
            venuesRepository.save(venues);
        }

        return ResponseEntity.<Void>noContent().build();
    }
}
