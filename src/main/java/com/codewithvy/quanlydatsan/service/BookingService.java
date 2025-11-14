package com.codewithvy.quanlydatsan.service;

import com.codewithvy.quanlydatsan.dto.BookingRejectRequest;
import com.codewithvy.quanlydatsan.dto.BookingRequest;
import com.codewithvy.quanlydatsan.dto.BookingResponse;
import com.codewithvy.quanlydatsan.dto.PaymentProofRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest bookingRequest);
    BookingResponse getBookingById(Long id);

    // API lấy tất cả booking
    List<BookingResponse> getMyBookings();
    List<BookingResponse> getAllBookingsForOwner();

    BookingResponse cancelBooking(Long id);

    // Phương thức mới cho workflow thanh toán
    BookingResponse confirmPayment(Long bookingId, PaymentProofRequest request);
    BookingResponse acceptBooking(Long bookingId);
    BookingResponse rejectBooking(Long bookingId, BookingRejectRequest request);
    List<BookingResponse> getVenueBookings(Long venueId);
    List<BookingResponse> getPendingBookingsForOwner();
    List<BookingResponse> getPendingBookingsByVenue(Long venueId);

    // Phương thức mới cho upload ảnh chuyển khoản
    BookingResponse uploadPaymentProof(Long bookingId, MultipartFile file);
}
