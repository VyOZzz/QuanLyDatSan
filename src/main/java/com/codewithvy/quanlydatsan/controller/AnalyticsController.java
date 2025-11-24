package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.dto.AnalyticsDTO.*;
import com.codewithvy.quanlydatsan.dto.ApiResponse;
import com.codewithvy.quanlydatsan.service.AnalyticsService;
import com.codewithvy.quanlydatsan.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "API phân tích và thống kê doanh thu cho chủ sân")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserService userService;

    @GetMapping("/analytics/owner")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
            summary = "API chính cho Analytics - Lấy dữ liệu thống kê tổng hợp",
            description = "Endpoint chính theo specs: Trả về toàn bộ dữ liệu analytics cho owner hiện tại trong 1 API call duy nhất"
    )
    public ResponseEntity<ApiResponse<AnalyticsData>> getAnalyticsForOwner(
            @Parameter(description = "Khoảng thời gian: DAY, WEEK, MONTH, YEAR", example = "MONTH")
            @RequestParam(defaultValue = "MONTH") String period,
            @Parameter(description = "Ngày bắt đầu (yyyy-MM-dd) - optional", example = "2025-11-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Ngày kết thúc (yyyy-MM-dd) - optional", example = "2025-11-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Long ownerId = userService.getCurrentUser().getId();
        AnalyticsData analytics = analyticsService.getAnalytics(ownerId, period, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok(analytics, "Lấy dữ liệu thống kê thành công"));
    }

    @GetMapping("/owners/{ownerId}/analytics")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
            summary = "Lấy dữ liệu phân tích tổng hợp cho chủ sân",
            description = "Trả về toàn bộ AnalyticsData bao gồm tổng quan, biểu đồ, top customers, venue performance, v.v. " +
                    "FE chỉ cần gọi 1 API này thay vì nhiều API và tự tính toán."
    )
    public ResponseEntity<ApiResponse<AnalyticsData>> getOwnerAnalytics(
            @Parameter(description = "ID chủ sân", example = "1")
            @PathVariable Long ownerId,
            @Parameter(description = "Khoảng thời gian: DAY, WEEK, MONTH, YEAR", example = "MONTH")
            @RequestParam(defaultValue = "MONTH") String period,
            @Parameter(description = "Ngày bắt đầu (yyyy-MM-dd) - optional", example = "2025-11-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Ngày kết thúc (yyyy-MM-dd) - optional", example = "2025-11-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Kiểm tra quyền: chỉ owner mới có thể xem analytics của chính mình
        Long currentUserId = userService.getCurrentUser().getId();
        if (!currentUserId.equals(ownerId)) {
            return ResponseEntity.status(403).body(
                    ApiResponse.fail("Bạn không có quyền xem thống kê của chủ sân khác")
            );
        }

        AnalyticsData analytics = analyticsService.getAnalytics(ownerId, period, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok(analytics, "Lấy dữ liệu phân tích thành công"));
    }

    @GetMapping("/owners/me/analytics")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
            summary = "Lấy dữ liệu phân tích cho chủ sân hiện tại",
            description = "Shortcut cho /owners/{ownerId}/analytics với ownerId là user hiện tại"
    )
    public ResponseEntity<ApiResponse<AnalyticsData>> getMyAnalytics(
            @Parameter(description = "Khoảng thời gian: DAY, WEEK, MONTH, YEAR", example = "MONTH")
            @RequestParam(defaultValue = "MONTH") String period,
            @Parameter(description = "Ngày bắt đầu (yyyy-MM-dd) - optional", example = "2025-11-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Ngày kết thúc (yyyy-MM-dd) - optional", example = "2025-11-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Long ownerId = userService.getCurrentUser().getId();
        AnalyticsData analytics = analyticsService.getAnalytics(ownerId, period, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok(analytics, "Lấy dữ liệu phân tích thành công"));
    }

    @GetMapping("/venues/{venueId}/analytics")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
            summary = "Lấy dữ liệu phân tích theo sân cụ thể",
            description = "Trả về analytics cho 1 sân cụ thể của chủ sân"
    )
    public ResponseEntity<ApiResponse<AnalyticsData>> getVenueAnalytics(
            @Parameter(description = "ID sân", example = "1")
            @PathVariable Long venueId,
            @Parameter(description = "Khoảng thời gian: DAY, WEEK, MONTH, YEAR", example = "MONTH")
            @RequestParam(defaultValue = "MONTH") String period,
            @Parameter(description = "Ngày bắt đầu (yyyy-MM-dd) - optional", example = "2025-11-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Ngày kết thúc (yyyy-MM-dd) - optional", example = "2025-11-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        AnalyticsData analytics = analyticsService.getVenueAnalytics(venueId, period, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok(analytics, "Lấy dữ liệu phân tích sân thành công"));
    }
}

