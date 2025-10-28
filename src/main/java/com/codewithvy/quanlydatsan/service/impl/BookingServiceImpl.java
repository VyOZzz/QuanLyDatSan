package com.codewithvy.quanlydatsan.service.impl;

import com.codewithvy.quanlydatsan.dto.*;
import com.codewithvy.quanlydatsan.entity.*;
import com.codewithvy.quanlydatsan.exception.ResourceNotFoundException;
import com.codewithvy.quanlydatsan.repository.BookingRepository;
import com.codewithvy.quanlydatsan.repository.CourtRepository;
import com.codewithvy.quanlydatsan.repository.UserRepository;
import com.codewithvy.quanlydatsan.security.UserDetailsImpl;
import com.codewithvy.quanlydatsan.service.BookingService;
import com.codewithvy.quanlydatsan.service.FileStorageService;
import com.codewithvy.quanlydatsan.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CourtRepository courtRepository;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;
    // Thời gian hết hạn thanh toán (5 phút) - có thể thay đổi theo nhu cầu
    private static final int PAYMENT_EXPIRE_MINUTES = 5;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest bookingRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Court court = courtRepository.findById(bookingRequest.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court not found"));

        // VALIDATION: Kiểm tra thời gian hợp lệ
        LocalDateTime now = LocalDateTime.now();

        if (bookingRequest.getStartTime().isBefore(now)) {
            throw new IllegalArgumentException("Thời gian bắt đầu phải sau thời điểm hiện tại");
        }
        if (bookingRequest.getEndTime().isBefore(bookingRequest.getStartTime())) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }
        if (bookingRequest.getEndTime().isBefore(now)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời điểm hiện tại");
        }

        // Kiểm tra sân đã bị đặt chưa (bao gồm cả các booking PENDING_PAYMENT)
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                bookingRequest.getCourtId(), bookingRequest.getStartTime(), bookingRequest.getEndTime());
        if (!overlappingBookings.isEmpty()) {
            throw new IllegalStateException("Sân đã được đặt trong khung giờ này. Vui lòng chọn khung giờ khác.");
        }

        // Tính giá tiền (đơn giản: 100,000 VND/giờ)
        long hours = Duration.between(bookingRequest.getStartTime(), bookingRequest.getEndTime()).toHours();
        double totalPrice = hours * 100000;

        // Tạo booking mới với status PENDING_PAYMENT
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setCourt(court);
        booking.setStartTime(bookingRequest.getStartTime());
        booking.setEndTime(bookingRequest.getEndTime());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setExpireTime(LocalDateTime.now().plusMinutes(PAYMENT_EXPIRE_MINUTES));
        booking.setPaymentProofUploaded(false);

        // KHÓA SÂN - Không ai đặt được trong khi pending
        court.setBooked(true);
        courtRepository.save(court);

        Booking savedBooking = bookingRepository.save(booking);
        return mapToBookingResponse(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponse confirmPayment(Long bookingId, PaymentProofRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        User currentUser = getCurrentUser();
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Bạn không có quyền confirm booking này");
        }

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Booking không ở trạng thái PENDING_PAYMENT");
        }

        if (LocalDateTime.now().isAfter(booking.getExpireTime())) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            throw new IllegalStateException("Booking đã hết hạn. Vui lòng tạo booking mới.");
        }

        // KIỂM TRA: User phải upload ảnh trước khi confirm
        if (!booking.getPaymentProofUploaded() || booking.getPaymentProofUrl() == null) {
            throw new IllegalStateException("Vui lòng upload ảnh chuyển khoản trước khi xác nhận thanh toán");
        }

        // Cập nhật status thành PAYMENT_UPLOADED
        booking.setStatus(BookingStatus.PAYMENT_UPLOADED);

        Booking savedBooking = bookingRepository.save(booking);

        // GỬI THÔNG BÁO CHO CHỦ SÂN
        User owner = booking.getCourt().getVenues().getOwner();
        notificationService.createNotification(
                owner,
                currentUser,
                booking,
                NotificationType.PAYMENT_UPLOADED,
                "Có khách đã chuyển khoản",
                String.format("Khách hàng %s đã chuyển khoản cho đơn đặt sân #%d tại %s. Vui lòng kiểm tra và xác nhận.",
                        currentUser.getFullname(), booking.getId(), booking.getCourt().getVenues().getName())
        );

        return mapToBookingResponse(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponse acceptBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        User currentUser = getCurrentUser();
        User owner = booking.getCourt().getVenues().getOwner();

        if (!owner.getId().equals(currentUser.getId())) {
            throw new SecurityException("You don't have permission to accept this booking");
        }

        if (booking.getStatus() != BookingStatus.PAYMENT_UPLOADED) {
            throw new IllegalStateException("Booking must be in PAYMENT_UPLOADED status to accept");
        }

        // Xác nhận đặt sân
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking savedBooking = bookingRepository.save(booking);

        // Gửi thông báo cho người đặt sân
        notificationService.createNotification(
                booking.getUser(),
                currentUser,
                booking,
                NotificationType.BOOKING_CONFIRMED,
                "Đặt sân thành công!",
                String.format("Đơn đặt sân #%d của bạn đã được chủ sân xác nhận. Chúc bạn chơi vui vẻ!",
                        booking.getId())
        );

        return mapToBookingResponse(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponse rejectBooking(Long bookingId, BookingRejectRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        User currentUser = getCurrentUser();
        User owner = booking.getCourt().getVenues().getOwner();

        if (!owner.getId().equals(currentUser.getId())) {
            throw new SecurityException("You don't have permission to reject this booking");
        }

        if (booking.getStatus() != BookingStatus.PAYMENT_UPLOADED) {
            throw new IllegalStateException("Booking must be in PAYMENT_UPLOADED status to reject");
        }

        // Từ chối đặt sân
        booking.setStatus(BookingStatus.REJECTED);
        booking.setRejectionReason(request.getRejectionReason());

        // GIẢI PHÓNG SÂN
        Court court = booking.getCourt();
        court.setBooked(false);
        courtRepository.save(court);

        Booking savedBooking = bookingRepository.save(booking);

        // Gửi thông báo cho người đặt sân
        notificationService.createNotification(
                booking.getUser(),
                currentUser,
                booking,
                NotificationType.BOOKING_REJECTED,
                "Đặt sân bị từ chối",
                String.format("Đơn đặt sân #%d của bạn đã bị từ chối. Lý do: %s",
                        booking.getId(), request.getRejectionReason())
        );

        return mapToBookingResponse(savedBooking);
    }

    @Override
    public List<BookingResponse> getVenueBookings(Long venueId) {
        List<Booking> bookings = bookingRepository.findByVenueId(venueId);
        return bookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getPendingBookingsForOwner() {
        User currentUser = getCurrentUser();
        List<Booking> bookings = bookingRepository.findPendingBookingsForOwner(currentUser.getId());
        return bookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return mapToBookingResponse(booking);
    }

    @Override
    public List<BookingResponse> getMyBookings() {
        User currentUser = getCurrentUser();
        List<Booking> bookings = bookingRepository.findByUserId(currentUser.getId());
        return bookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        User currentUser = getCurrentUser();
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You are not authorized to cancel this booking");
        }

        // Chỉ cho phép cancel nếu chưa confirmed
        if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel confirmed or completed booking");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        // GIẢI PHÓNG SÂN
        Court court = booking.getCourt();
        court.setBooked(false);
        courtRepository.save(court);

        Booking updatedBooking = bookingRepository.save(booking);
        return mapToBookingResponse(updatedBooking);
    }

    @Override
    @Transactional
    public BookingResponse uploadPaymentProof(Long bookingId, MultipartFile file) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        User currentUser = getCurrentUser();
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Bạn không có quyền upload ảnh cho booking này");
        }

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Chỉ có thể upload ảnh khi booking đang ở trạng thái PENDING_PAYMENT");
        }

        if (LocalDateTime.now().isAfter(booking.getExpireTime())) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            throw new IllegalStateException("Booking đã hết hạn. Vui lòng tạo booking mới.");
        }

        // Xóa ảnh cũ nếu có
        if (booking.getPaymentProofUrl() != null) {
            fileStorageService.deletePaymentProofImage(booking.getPaymentProofUrl());
        }

        // Lưu file mới
        String fileUrl = fileStorageService.savePaymentProofImage(file, bookingId);

        // Cập nhật booking - CHỈ LƯU ẢNH, CHƯA THAY ĐỔI STATUS
        booking.setPaymentProofUrl(fileUrl);
        booking.setPaymentProofUploaded(true);
        booking.setPaymentProofUploadedAt(LocalDateTime.now());
        // KHÔNG thay đổi status ở đây - vẫn giữ PENDING_PAYMENT

        Booking savedBooking = bookingRepository.save(booking);

        // KHÔNG gửi thông báo ở đây - chờ user nhấn Confirm Payment

        return mapToBookingResponse(savedBooking);
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        BookingResponse.BookingResponseBuilder builder = BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .userName(booking.getUser().getFullname())
                .courtId(booking.getCourt().getId())
                .courtName("Sân " + booking.getCourt().getId())
                .venuesName(booking.getCourt().getVenues().getName())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .expireTime(booking.getExpireTime())
                .paymentProofUploaded(booking.getPaymentProofUploaded())
                .paymentProofUrl(booking.getPaymentProofUrl())
                .paymentProofUploadedAt(booking.getPaymentProofUploadedAt())
                .rejectionReason(booking.getRejectionReason());

        // Thêm thông tin tài khoản chủ sân nếu status là PENDING_PAYMENT
        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
            User owner = booking.getCourt().getVenues().getOwner();
            OwnerBankInfoDTO bankInfo = OwnerBankInfoDTO.builder()
                    .bankName(owner.getBankName())
                    .bankAccountNumber(owner.getBankAccountNumber())
                    .bankAccountName(owner.getBankAccountName())
                    .ownerName(owner.getFullname())
                    .build();
            builder.ownerBankInfo(bankInfo);
        }

        return builder.build();
    }

    private User getCurrentUser() {
        String phone = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
