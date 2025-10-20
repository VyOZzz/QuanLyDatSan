package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.dto.*;
import com.codewithvy.quanlydatsan.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
        BookingResponse bookingResponse = bookingService.createBooking(bookingRequest);
        return ResponseEntity.ok(ApiResponse.ok(bookingResponse, "Đặt sân thành công. Vui lòng chuyển khoản trong 15 phút."));
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
}
