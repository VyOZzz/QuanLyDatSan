package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.dto.ApiResponse;
import com.codewithvy.quanlydatsan.dto.VenuesDTO;
import com.codewithvy.quanlydatsan.dto.VenuesRequest;
import com.codewithvy.quanlydatsan.service.VenuesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@Tag(name = "Venues", description = "API quản lý địa điểm/sân thể thao")
public class VenuesController {
    private static final Logger log = LoggerFactory.getLogger(VenuesController.class);

    private final VenuesService venuesService;

    public VenuesController(VenuesService venuesService) {
        this.venuesService = venuesService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Lấy danh sách tất cả venues",
        description = "Trả về danh sách tất cả các venues trong hệ thống (yêu cầu đăng nhập)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<List<VenuesDTO>>> getAllVenues() {
        return ResponseEntity.ok(ApiResponse.ok(venuesService.getAll(), "List venues"));
    }

    /**
     * Tìm kiếm theo tên và/hoặc địa chỉ. Tham số đều là tuỳ chọn.
     * VD: /api/venues/search?name=abc&province=Hanoi
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Tìm kiếm venues",
        description = "Tìm kiếm venues theo tên và/hoặc địa chỉ (tỉnh, quận, chi tiết). Tất cả tham số đều tùy chọn (yêu cầu đăng nhập)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<List<VenuesDTO>>> searchVenues(
            @Parameter(description = "Tên venue cần tìm") @RequestParam(name = "name", required = false) String name,
            @Parameter(description = "Tỉnh/Thành phố") @RequestParam(name = "province", required = false) String province,
            @Parameter(description = "Quận/Huyện") @RequestParam(name = "district", required = false) String district,
            @Parameter(description = "Địa chỉ chi tiết") @RequestParam(name = "detail", required = false) String detail) {
        List<VenuesDTO> results = venuesService.search(name, province, district, detail);
        return ResponseEntity.ok(ApiResponse.ok(results, "Search results"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Lấy thông tin venue theo ID",
        description = "Trả về thông tin chi tiết của một venue (yêu cầu đăng nhập)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<VenuesDTO>> getVenuesById(
            @Parameter(description = "ID của venue", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(venuesService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
        summary = "Tạo venue mới",
        description = "Tạo một venue mới (yêu cầu ROLE_OWNER)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
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
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
        summary = "Cập nhật venue",
        description = "Cập nhật thông tin venue theo ID (yêu cầu ROLE_OWNER)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<VenuesDTO>> updateVenues(
            @Parameter(description = "ID của venue cần cập nhật", required = true) @PathVariable Long id,
            @RequestBody VenuesRequest request) {
        VenuesDTO updated = venuesService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
        summary = "Xóa venue",
        description = "Xóa venue theo ID (yêu cầu ROLE_OWNER)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> deleteVenues(
            @Parameter(description = "ID của venue cần xóa", required = true) @PathVariable Long id) {
        venuesService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Deleted"));
    }
}
