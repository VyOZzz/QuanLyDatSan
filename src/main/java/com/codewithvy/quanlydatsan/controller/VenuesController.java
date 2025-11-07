package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.dto.ApiResponse;
import com.codewithvy.quanlydatsan.dto.VenuesDTO;
import com.codewithvy.quanlydatsan.dto.VenuesRequest;
import com.codewithvy.quanlydatsan.entity.BookingItem;
import com.codewithvy.quanlydatsan.entity.Court;
import com.codewithvy.quanlydatsan.repository.BookingItemRepository;
import com.codewithvy.quanlydatsan.repository.CourtRepository;
import com.codewithvy.quanlydatsan.service.VenuesService;
import com.codewithvy.quanlydatsan.service.FileStorageService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/venues")
@Tag(name = "Venues", description = "API quản lý địa điểm/sân thể thao")
public class VenuesController {
    private static final Logger log = LoggerFactory.getLogger(VenuesController.class);

    private final VenuesService venuesService;
    private final FileStorageService fileStorageService;
    private final CourtRepository courtRepository;
    private final BookingItemRepository bookingItemRepository;

    public VenuesController(VenuesService venuesService, FileStorageService fileStorageService,
                           CourtRepository courtRepository, BookingItemRepository bookingItemRepository) {
        this.venuesService = venuesService;
        this.fileStorageService = fileStorageService;
        this.courtRepository = courtRepository;
        this.bookingItemRepository = bookingItemRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Ly danh sch tt c venues",
        description = "Tr v danh sch tt c cc venues trong h‡ th'ng (yu cu 'ƒng nhp)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<List<VenuesDTO>>> getAllVenues() {
        return ResponseEntity.ok(ApiResponse.ok(venuesService.getAll(), "List venues"));
    }

    @GetMapping("/my-venues")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
        summary = "Lấy danh sách venues của tôi",
        description = "Trả về danh sách các venues thuộc sở hữu của owner đang đăng nhập (yêu cầu ROLE_OWNER)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<List<VenuesDTO>>> getMyVenues() {
        return ResponseEntity.ok(ApiResponse.ok(venuesService.getMyVenues(), "My venues"));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Tìm kiếm venues - 1 ô duy nhất",
        description = "Tìm kiếm venues trong tất cả các trường (tên, tỉnh, quận, địa chỉ) bằng 1 ô input duy nhất. Dễ sử dụng cho search box đơn giản. (yêu cầu đăng nhập)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<List<VenuesDTO>>> searchVenues(
            @Parameter(description = "Từ khóa tìm kiếm (tìm trong tên, tỉnh, quận, địa chỉ)") @RequestParam(name = "q", required = false) String query) {
        List<VenuesDTO> results = venuesService.searchUnified(query);
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

    @GetMapping("/{venueId}/courts")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Lấy danh sách courts của venue (đơn giản)",
        description = "Trả về danh sách tất cả courts của venue (KHÔNG bao gồm thông tin availability/booked slots). Sử dụng khi chỉ cần hiển thị danh sách sân đơn giản. (yêu cầu đăng nhập)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCourtsByVenue(
            @Parameter(description = "ID của venue", required = true) @PathVariable Long venueId) {

        // Kiểm tra venue tồn tại
        VenuesDTO venue = venuesService.getById(venueId);

        // Lấy danh sách courts của venue (query trực tiếp từ DB)
        List<Court> courts = courtRepository.findByVenuesId(venueId);

        log.info("Found {} courts for venue {} ({})", courts.size(), venueId, venue.getName());

        // Map sang response đơn giản (chỉ thông tin cơ bản)
        List<Map<String, Object>> courtsList = courts.stream().map(court -> {
            Map<String, Object> courtInfo = new HashMap<>();
            courtInfo.put("id", court.getId());
            courtInfo.put("description", court.getDescription());
            courtInfo.put("isActive", court.getIsActive()); // ← Thêm trạng thái sân
            courtInfo.put("venueId", venueId);
            courtInfo.put("venueName", venue.getName());
            return courtInfo;
        }).collect(Collectors.toList());

        log.info("Returning {} courts for venue {}", courtsList.size(), venueId);

        return ResponseEntity.ok(ApiResponse.ok(courtsList, "Courts list"));
    }

    @GetMapping("/{venueId}/courts/availability")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Lấy danh sách courts của venue với trạng thái availability",
        description = """
            Trả về danh sách tất cả courts của venue kèm theo trạng thái available/unavailable trong khoảng thời gian cụ thể
            
            ⏰ **THỜI GIAN:**
            - Format: `yyyy-MM-dd'T'HH:mm:ss` (VD: `2025-11-07T14:00:00`)
            - Múi giờ: Việt Nam (Asia/Ho_Chi_Minh, UTC+7)
            - **KHÔNG CẦN** thêm `Z` hoặc `+07:00`
            
            📅 **CHO FRONTEND:**
            - Response bao gồm `openingTime` và `closingTime` để frontend chia thành các ô 30 phút
            - `bookedSlots` chứa danh sách các slot đã được đặt
            - Frontend dựa vào 2 thông tin này để render lịch với trạng thái từng ô
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCourtsWithAvailability(
            @Parameter(description = "ID của venue", required = true)
            @PathVariable Long venueId,
            @Parameter(
                description = "Thời gian bắt đầu kiểm tra (Giờ Việt Nam)",
                required = true,
                example = "2025-11-07T14:00:00"
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(
                description = "Thời gian kết thúc kiểm tra (Giờ Việt Nam)",
                required = true,
                example = "2025-11-07T15:00:00"
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        // Kiểm tra venue tồn tại
        VenuesDTO venue = venuesService.getById(venueId);

        // Lấy danh sách courts của venue (query trực tiếp từ DB)
        List<Court> courts = courtRepository.findByVenuesId(venueId);

        log.info("Checking availability for {} courts of venue {}", courts.size(), venueId);

        // Map sang response với trạng thái availability
        List<Map<String, Object>> courtsList = courts.stream().map(court -> {
            // Kiểm tra court có bị đặt trong khoảng thời gian không
            List<BookingItem> bookedSlots = bookingItemRepository.findBookedSlots(
                court.getId(), startTime, endTime
            );

            Map<String, Object> courtInfo = new HashMap<>();
            courtInfo.put("id", court.getId());
            courtInfo.put("description", court.getDescription());
            courtInfo.put("isActive", court.getIsActive()); // ← Trạng thái sân
            courtInfo.put("available", bookedSlots.isEmpty()); // ← Trạng thái available

            // Thêm thông tin các slot đã đặt (nếu có)
            if (!bookedSlots.isEmpty()) {
                List<Map<String, Object>> slots = bookedSlots.stream().map(slot -> {
                    Map<String, Object> slotInfo = new HashMap<>();
                    slotInfo.put("startTime", slot.getStartTime());
                    slotInfo.put("endTime", slot.getEndTime());
                    slotInfo.put("bookingId", slot.getBooking().getId());
                    return slotInfo;
                }).collect(Collectors.toList());
                courtInfo.put("bookedSlots", slots);
            } else {
                courtInfo.put("bookedSlots", new ArrayList<>());
            }

            return courtInfo;
        }).collect(Collectors.toList());

        // Thêm thông tin venue vào response (bao gồm giờ mở/đóng cửa)
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("venueId", venueId);
        responseData.put("venueName", venue.getName());
        responseData.put("openingTime", venue.getOpeningTime()); // ← Giờ mở cửa (VD: "06:00:00")
        responseData.put("closingTime", venue.getClosingTime()); // ← Giờ đóng cửa (VD: "23:00:00")
        responseData.put("pricePerHour", venue.getPricePerHour()); // ← Giá theo giờ
        responseData.put("timeRange", Map.of(
            "startTime", startTime,
            "endTime", endTime
        ));
        responseData.put("courts", courtsList);
        responseData.put("totalCourts", courtsList.size());
        responseData.put("availableCourts", courtsList.stream().filter(c -> (Boolean) c.get("available")).count());

        log.info("Checked availability for venue {}: {} courts, {} available, openingTime={}, closingTime={}",
            venueId, courtsList.size(), responseData.get("availableCourts"),
            venue.getOpeningTime(), venue.getClosingTime());

        return ResponseEntity.ok(ApiResponse.ok(responseData, "Courts with availability and venue info"));
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

    @PostMapping("/{id}/upload-images")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
        summary = "Upload ảnh cho venue",
        description = "Upload một hoặc nhiều ảnh cho venue (yêu cầu ROLE_OWNER và phải là chủ sở hữu venue)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<List<String>>> uploadVenueImages(
            @Parameter(description = "ID của venue", required = true) @PathVariable Long id,
            @Parameter(description = "Các file ảnh cần upload", required = true)
            @RequestParam("images") MultipartFile[] images) {
        try {
            log.info("POST /api/venues/{}/upload-images - Uploading {} images", id, images.length);

            // Kiểm tra venue tồn tại và quyền sở hữu (sẽ throw exception nếu không hợp lệ)
            VenuesDTO venue = venuesService.getById(id);

            // Validate ownership bằng cách thử update (chỉ owner mới được phép)
            // Hoặc có thể thêm method riêng checkOwnership trong service

            List<String> uploadedUrls = new ArrayList<>();
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String imageUrl = fileStorageService.saveVenueImage(image, id);
                    uploadedUrls.add(imageUrl);
                    log.info("Uploaded image: {}", imageUrl);
                }
            }

            // Cập nhật danh sách ảnh vào venue
            VenuesRequest updateRequest = new VenuesRequest();
            List<String> currentImages = venue.getImages() != null ? new ArrayList<>(venue.getImages()) : new ArrayList<>();
            currentImages.addAll(uploadedUrls);
            updateRequest.setImages(currentImages);

            venuesService.update(id, updateRequest);

            log.info("Successfully uploaded {} images for venue id: {}", uploadedUrls.size(), id);
            return ResponseEntity.ok(ApiResponse.ok(uploadedUrls, "Images uploaded successfully"));
        } catch (Exception e) {
            log.error("Error uploading images for venue {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{id}/delete-image")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
        summary = "Xóa ảnh của venue",
        description = "Xóa một ảnh cụ thể của venue (yêu cầu ROLE_OWNER và phải là chủ sở hữu venue)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> deleteVenueImage(
            @Parameter(description = "ID của venue", required = true) @PathVariable Long id,
            @Parameter(description = "URL của ảnh cần xóa", required = true)
            @RequestParam("imageUrl") String imageUrl) {
        try {
            log.info("DELETE /api/venues/{}/delete-image - Deleting image: {}", id, imageUrl);

            // Lấy thông tin venue
            VenuesDTO venue = venuesService.getById(id);

            // Xóa ảnh khỏi danh sách
            if (venue.getImages() != null && venue.getImages().contains(imageUrl)) {
                List<String> updatedImages = new ArrayList<>(venue.getImages());
                updatedImages.remove(imageUrl);

                VenuesRequest updateRequest = new VenuesRequest();
                updateRequest.setImages(updatedImages);
                venuesService.update(id, updateRequest);

                // Xóa file vật lý
                fileStorageService.deleteVenueImage(imageUrl);

                log.info("Successfully deleted image for venue id: {}", id);
                return ResponseEntity.ok(ApiResponse.ok(null, "Image deleted successfully"));
            } else {
                throw new IllegalArgumentException("Image not found in venue");
            }
        } catch (Exception e) {
            log.error("Error deleting image for venue {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
