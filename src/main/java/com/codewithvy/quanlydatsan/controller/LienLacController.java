package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.dto.ApiResponse;
import com.codewithvy.quanlydatsan.dto.LienLacDTO;
import com.codewithvy.quanlydatsan.dto.LienLacRequest;
import com.codewithvy.quanlydatsan.dto.LienLacUpdateRequest;
import com.codewithvy.quanlydatsan.service.LienLacService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
@Tag(name = "Contacts (Liên Lạc)", description = "API quản lý liên lạc/contact từ người dùng")
public class LienLacController {

    private final LienLacService lienLacService;

    @PostMapping
    @Operation(
            summary = "Tạo liên lạc mới",
            description = "Cho phép user hoặc khách tạo liên lạc mới. Nếu đã đăng nhập thì tự động gắn user."
    )
    public ResponseEntity<ApiResponse<LienLacDTO>> createLienLac(
            @Valid @RequestBody LienLacRequest request,
            Authentication authentication
    ) {
        String userPhone = authentication != null ? authentication.getName() : null;
        LienLacDTO lienLac = lienLacService.createLienLac(request, userPhone);
        return ResponseEntity.ok(ApiResponse.ok(lienLac, "Tạo liên lạc thành công"));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Lấy danh sách liên lạc của tôi",
            description = "Lấy tất cả liên lạc mà user hiện tại đã tạo"
    )
    public ResponseEntity<ApiResponse<List<LienLacDTO>>> getMyLienLac(
            Authentication authentication
    ) {
        String userPhone = authentication.getName();
        List<LienLacDTO> lienLacs = lienLacService.getMyLienLac(userPhone);
        return ResponseEntity.ok(ApiResponse.ok(lienLacs, "Lấy danh sách liên lạc thành công"));
    }

    @GetMapping("/owner")
    @PreAuthorize("hasRole('OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Lấy danh sách liên lạc cho owner",
            description = "Lấy tất cả liên lạc liên quan đến các venue của owner hiện tại"
    )
    public ResponseEntity<ApiResponse<List<LienLacDTO>>> getLienLacForOwner(
            Authentication authentication
    ) {
        String userPhone = authentication.getName();
        List<LienLacDTO> lienLacs = lienLacService.getLienLacByOwner(userPhone);
        return ResponseEntity.ok(ApiResponse.ok(lienLacs, "Lấy danh sách liên lạc thành công"));
    }

    @GetMapping("/venue/{venueId}")
    @PreAuthorize("hasRole('OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Lấy danh sách liên lạc theo venue",
            description = "Lấy tất cả liên lạc liên quan đến một venue cụ thể (chỉ owner của venue đó)"
    )
    public ResponseEntity<ApiResponse<List<LienLacDTO>>> getLienLacByVenue(
            @Parameter(description = "ID của venue", example = "1")
            @PathVariable Long venueId,
            Authentication authentication
    ) {
        String userPhone = authentication.getName();
        List<LienLacDTO> lienLacs = lienLacService.getLienLacByVenue(venueId, userPhone);
        return ResponseEntity.ok(ApiResponse.ok(lienLacs, "Lấy danh sách liên lạc thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Lấy chi tiết một liên lạc",
            description = "Lấy thông tin chi tiết của một liên lạc (chỉ người tạo hoặc owner của venue liên quan)"
    )
    public ResponseEntity<ApiResponse<LienLacDTO>> getLienLacById(
            @Parameter(description = "ID của liên lạc", example = "1")
            @PathVariable Long id,
            Authentication authentication
    ) {
        String userPhone = authentication.getName();
        LienLacDTO lienLac = lienLacService.getLienLacById(id, userPhone);
        return ResponseEntity.ok(ApiResponse.ok(lienLac, "Lấy thông tin liên lạc thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Cập nhật trạng thái và phản hồi liên lạc",
            description = "Cho phép owner cập nhật trạng thái và thêm phản hồi cho liên lạc"
    )
    public ResponseEntity<ApiResponse<LienLacDTO>> updateLienLac(
            @Parameter(description = "ID của liên lạc", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody LienLacUpdateRequest request,
            Authentication authentication
    ) {
        String userPhone = authentication.getName();
        LienLacDTO lienLac = lienLacService.updateLienLac(id, request, userPhone);
        return ResponseEntity.ok(ApiResponse.ok(lienLac, "Cập nhật liên lạc thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Xóa liên lạc",
            description = "Cho phép owner xóa liên lạc (chỉ owner của venue liên quan)"
    )
    public ResponseEntity<ApiResponse<Void>> deleteLienLac(
            @Parameter(description = "ID của liên lạc", example = "1")
            @PathVariable Long id,
            Authentication authentication
    ) {
        String userPhone = authentication.getName();
        lienLacService.deleteLienLac(id, userPhone);
        return ResponseEntity.ok(ApiResponse.ok(null, "Xóa liên lạc thành công"));
    }
}
