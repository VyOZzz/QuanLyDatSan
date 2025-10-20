package com.codewithvy.quanlydatsan.service;

import com.codewithvy.quanlydatsan.dto.BookingRejectRequest;
import com.codewithvy.quanlydatsan.dto.BookingRequest;
import com.codewithvy.quanlydatsan.dto.BookingResponse;
import com.codewithvy.quanlydatsan.dto.PaymentProofRequest;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest bookingRequest);
    BookingResponse getBookingById(Long id);
    List<BookingResponse> getMyBookings();
    BookingResponse cancelBooking(Long id);

    // Phương thức mới cho workflow thanh toán
    BookingResponse confirmPayment(Long bookingId, PaymentProofRequest request);
    BookingResponse acceptBooking(Long bookingId);
    BookingResponse rejectBooking(Long bookingId, BookingRejectRequest request);
    List<BookingResponse> getVenueBookings(Long venueId);
    List<BookingResponse> getPendingBookingsForOwner();
}
