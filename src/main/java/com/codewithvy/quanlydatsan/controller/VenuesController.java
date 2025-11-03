package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.dto.ApiResponse;
import com.codewithvy.quanlydatsan.dto.VenuesDTO;
import com.codewithvy.quanlydatsan.dto.VenuesRequest;
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

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/venues")
@Tag(name = "Venues", description = "API quản lý địa điểm/sân thể thao")
public class VenuesController {
    private static final Logger log = LoggerFactory.getLogger(VenuesController.class);

    private final VenuesService venuesService;
    private final FileStorageService fileStorageService;

    public VenuesController(VenuesService venuesService, FileStorageService fileStorageService) {
        this.venuesService = venuesService;
        this.fileStorageService = fileStorageService;
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

    /**
     * Tm kim theo tn v/hoc '‹a ch‰. Tham s' 'u l tu chn.
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
