package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.dto.ApiResponse;
import com.codewithvy.quanlydatsan.dto.VenuesDTO;
import com.codewithvy.quanlydatsan.dto.VenuesRequest;
import com.codewithvy.quanlydatsan.service.VenuesService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
public class VenuesController {
    private static final Logger log = LoggerFactory.getLogger(VenuesController.class);

    private final VenuesService venuesService;

    public VenuesController(VenuesService venuesService) {
        this.venuesService = venuesService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VenuesDTO>>> getAllVenues() {
        return ResponseEntity.ok(ApiResponse.ok(venuesService.getAll(), "List venues"));
    }

    /**
     * Tìm kiếm theo tên và/hoặc địa chỉ. Tham số đều là tuỳ chọn.
     * VD: /api/venues/search?name=abc&province=Hanoi
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<VenuesDTO>>> searchVenues(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "province", required = false) String province,
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "detail", required = false) String detail) {
        List<VenuesDTO> results = venuesService.search(name, province, district, detail);
        return ResponseEntity.ok(ApiResponse.ok(results, "Search results"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VenuesDTO>> getVenuesById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(venuesService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VenuesDTO>> createVenues(@Valid @RequestBody VenuesRequest request) {
        try {
            log.info("POST /api/venues - Request received: name={}, address={}",
                request.getName(),
                request.getAddress() != null ? request.getAddress().getProvinceOrCity() : "null");

            VenuesDTO created = venuesService.create(request);

            log.info("Venue created successfully with id: {}", created.getId());
            return ResponseEntity.ok(ApiResponse.ok(created, "Created"));
        } catch (Exception e) {
            log.error("Error creating venue: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VenuesDTO>> updateVenues(@PathVariable Long id, @RequestBody VenuesRequest request) {
        VenuesDTO updated = venuesService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVenues(@PathVariable Long id) {
        venuesService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Deleted"));
    }
}
