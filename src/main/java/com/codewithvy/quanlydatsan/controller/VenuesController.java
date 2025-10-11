package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.dto.ApiResponse;
import com.codewithvy.quanlydatsan.dto.VenuesDTO;
import com.codewithvy.quanlydatsan.dto.VenuesDetailDTO;
import com.codewithvy.quanlydatsan.dto.VenuesRequest;
import com.codewithvy.quanlydatsan.service.VenuesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
public class VenuesController {

    private final VenuesService venuesService;

    public VenuesController(VenuesService venuesService) {
        this.venuesService = venuesService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VenuesDTO>>> getAllVenues(@RequestParam(value = "provinceOrCity", required = false) String provinceOrCity) {
        List<VenuesDTO> data = (provinceOrCity == null || provinceOrCity.isBlank())
                ? venuesService.getAll()
                : venuesService.getAll(provinceOrCity);
        return ResponseEntity.ok(ApiResponse.ok(data, "List venues"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VenuesDetailDTO>> getVenuesById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(venuesService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VenuesDTO>> createVenues(@RequestBody VenuesRequest request) {
        VenuesDTO created = venuesService.create(request);
        return ResponseEntity.ok(ApiResponse.ok(created, "Created"));
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
