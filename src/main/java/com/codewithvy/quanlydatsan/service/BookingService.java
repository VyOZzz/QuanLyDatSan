package com.codewithvy.quanlydatsan.service;

import com.codewithvy.quanlydatsan.dto.BookingRequest;
import com.codewithvy.quanlydatsan.dto.BookingResponse;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest bookingRequest);
    BookingResponse getBookingById(Long id);
    List<BookingResponse> getMyBookings();
    BookingResponse cancelBooking(Long id);
}

