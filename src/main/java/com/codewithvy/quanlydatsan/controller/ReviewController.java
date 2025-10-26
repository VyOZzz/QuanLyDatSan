package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.dto.ApiResponse;
import com.codewithvy.quanlydatsan.dto.ReviewDTO;
import com.codewithvy.quanlydatsan.dto.ReviewRequest;
import com.codewithvy.quanlydatsan.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Review", description = "API quản lý đánh giá và xếp hạng")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Tạo review mới cho một booking đã hoàn thành.
     * Chỉ USER mới có thể tạo review.
     */
    @PostMapping("/bookings/{bookingId}/review")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tạo review cho booking đã hoàn thành",
               description = "USER tạo đánh giá cho booking của mình sau khi hoàn thành")
    public ResponseEntity<ApiResponse<ReviewDTO>> createReview(
            @PathVariable Long bookingId,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {

        String userPhone = authentication.getName();
        ReviewDTO review = reviewService.createReview(bookingId, request, userPhone);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ReviewDTO>builder()
                        .success(true)
                        .message("Review created successfully")
                        .data(review)
                        .build());
    }

    /**
     * Lấy tất cả review của một venue (công khai).
     */
    @GetMapping("/venues/{venueId}/reviews")
    @Operation(summary = "Lấy tất cả review của một venue",
               description = "API công khai để xem đánh giá của một trung tâm")
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getVenueReviews(@PathVariable Long venueId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByVenue(venueId);

        return ResponseEntity.ok(ApiResponse.<List<ReviewDTO>>builder()
                .success(true)
                .message("Reviews retrieved successfully")
                .data(reviews)
                .build());
    }

    /**
     * Lấy tất cả review của user hiện tại.
     */
    @GetMapping("/my-reviews")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lấy tất cả review của user",
               description = "USER xem lại các đánh giá đã tạo")
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getMyReviews(Authentication authentication) {
        String userPhone = authentication.getName();
        List<ReviewDTO> reviews = reviewService.getReviewsByUser(userPhone);

        return ResponseEntity.ok(ApiResponse.<List<ReviewDTO>>builder()
                .success(true)
                .message("Your reviews retrieved successfully")
                .data(reviews)
                .build());
    }

    /**
     * Lấy review của một booking cụ thể.
     */
    @GetMapping("/bookings/{bookingId}/review")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lấy review của một booking",
               description = "USER xem review của booking cụ thể")
    public ResponseEntity<ApiResponse<ReviewDTO>> getBookingReview(
            @PathVariable Long bookingId,
            Authentication authentication) {

        String userPhone = authentication.getName();
        ReviewDTO review = reviewService.getReviewByBooking(bookingId, userPhone);

        return ResponseEntity.ok(ApiResponse.<ReviewDTO>builder()
                .success(true)
                .message("Review retrieved successfully")
                .data(review)
                .build());
    }

    /**
     * Xóa review của user.
     */
    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Xóa review",
               description = "USER xóa review của chính mình")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {

        String userPhone = authentication.getName();
        reviewService.deleteReview(reviewId, userPhone);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Review deleted successfully")
                .build());
    }
}

