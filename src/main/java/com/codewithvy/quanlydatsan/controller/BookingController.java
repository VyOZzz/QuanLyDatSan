package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.dto.*;
import com.codewithvy.quanlydatsan.service.BookingService;
import com.codewithvy.quanlydatsan.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final FileStorageService fileStorageService;

    public BookingController(BookingService bookingService, FileStorageService fileStorageService) {
        this.bookingService = bookingService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
        BookingResponse bookingResponse = bookingService.createBooking(bookingRequest);
        return ResponseEntity.ok(ApiResponse.ok(bookingResponse, "Đặt sân thành công. Vui lòng chuyển khoản trong 5 phút."));
    }

    @PutMapping("/{id}/confirm-payment")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentProofRequest request) {
        BookingResponse bookingResponse = bookingService.confirmPayment(id, request);
        return ResponseEntity.ok(ApiResponse.ok(bookingResponse, "Đã gửi chứng minh chuyển khoản. Chờ chủ sân xác nhận."));
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<BookingResponse>> acceptBooking(@PathVariable Long id) {
        BookingResponse bookingResponse = bookingService.acceptBooking(id);
        return ResponseEntity.ok(ApiResponse.ok(bookingResponse, "Đã xác nhận đặt sân thành công."));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<BookingResponse>> rejectBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingRejectRequest request) {
        BookingResponse bookingResponse = bookingService.rejectBooking(id, request);
        return ResponseEntity.ok(ApiResponse.ok(bookingResponse, "Đã từ chối đặt sân."));
    }

    @GetMapping("/venue/{venueId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getVenueBookings(@PathVariable Long venueId) {
        List<BookingResponse> bookings = bookingService.getVenueBookings(venueId);
        return ResponseEntity.ok(ApiResponse.ok(bookings, "Lấy danh sách booking thành công."));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getPendingBookings() {
        List<BookingResponse> bookings = bookingService.getPendingBookingsForOwner();
        return ResponseEntity.ok(ApiResponse.ok(bookings, "Lấy danh sách booking chờ xác nhận thành công."));
    }

    @GetMapping("/venue/{venueId}/pending")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Lấy danh sách booking chờ xác nhận theo venue",
               description = "Trả về các booking có status PAYMENT_UPLOADED của một venue cụ thể")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getPendingBookingsByVenue(@PathVariable Long venueId) {
        List<BookingResponse> bookings = bookingService.getPendingBookingsByVenue(venueId);
        return ResponseEntity.ok(ApiResponse.ok(bookings, "Lấy danh sách booking chờ xác nhận của venue thành công."));
    }

    @GetMapping("/owner/all-bookings")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Lấy tất cả booking của các sân thuộc sở hữu",
               description = "Trả về tất cả booking tại các venues thuộc sở hữu của owner (tất cả trạng thái)")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookingsForOwner() {
        List<BookingResponse> bookings = bookingService.getAllBookingsForOwner();
        return ResponseEntity.ok(ApiResponse.ok(bookings, "Lấy danh sách tất cả booking thành công."));
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings() {
        List<BookingResponse> bookings = bookingService.getMyBookings();
        return ResponseEntity.ok(ApiResponse.ok(bookings, "My bookings retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        BookingResponse booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.ok(booking, "Booking retrieved successfully"));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable Long id) {
        BookingResponse bookingResponse = bookingService.cancelBooking(id);
        return ResponseEntity.ok(ApiResponse.ok(bookingResponse, "Booking cancelled successfully"));
    }

    @PostMapping("/{id}/upload-payment-proof")
    @PreAuthorize("hasRole('USER')")
    @Operation(
        summary = "Upload ảnh chứng minh chuyển khoản",
        description = "Upload ảnh chụp màn hình đã chuyển khoản. Chỉ chấp nhận file ảnh jpg, jpeg, png (max 10MB)"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = @Content(
            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
            schema = @Schema(implementation = FileUploadRequest.class)
        )
    )
    public ResponseEntity<ApiResponse<BookingResponse>> uploadPaymentProof(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        BookingResponse bookingResponse = bookingService.uploadPaymentProof(id, file);
        return ResponseEntity.ok(ApiResponse.ok(bookingResponse,
            "Đã upload ảnh thành công. Vui lòng nhấn 'Xác nhận thanh toán' để gửi cho chủ sân."));
    }
}
