package com.codewithvy.quanlydatsan.service.impl;

import com.codewithvy.quanlydatsan.dto.*;
import com.codewithvy.quanlydatsan.entity.*;
import com.codewithvy.quanlydatsan.exception.ResourceNotFoundException;
import com.codewithvy.quanlydatsan.repository.BookingRepository;
import com.codewithvy.quanlydatsan.repository.BookingItemRepository;
import com.codewithvy.quanlydatsan.repository.CourtRepository;
import com.codewithvy.quanlydatsan.repository.UserRepository;
import com.codewithvy.quanlydatsan.security.UserDetailsImpl;
import com.codewithvy.quanlydatsan.service.BookingService;
import com.codewithvy.quanlydatsan.service.FileStorageService;
import com.codewithvy.quanlydatsan.service.NotificationService;
import com.codewithvy.quanlydatsan.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final UserRepository userRepository;
    private final CourtRepository courtRepository;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;
    private final PriceService priceService;

    // Thời gian hết hạn thanh toán (5 phút) - có thể thay đổi theo nhu cầu
    private static final int PAYMENT_EXPIRE_MINUTES = 5;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest bookingRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.info("Creating booking for user={}, courtId={}, venueId={}, startTime={}, endTime={}",
            user.getEmail(),
            bookingRequest.getCourtId(),
            bookingRequest.getVenueId(),
            bookingRequest.getStartTime(),
            bookingRequest.getEndTime());

        // VALIDATION 1: Kiểm tra court tồn tại
        Court court = courtRepository.findById(bookingRequest.getCourtId())
                .orElseThrow(() -> {
                    log.error("Court not found with id: {}", bookingRequest.getCourtId());
                    return new ResourceNotFoundException(
                        String.format("Không tìm thấy sân với ID: %d. Sân có thể đã bị xóa hoặc không tồn tại.",
                            bookingRequest.getCourtId())
                    );
                });

        log.info("Found court: id={}, description='{}', isActive={}, venueId={}, venueName='{}'",
            court.getId(),
            court.getDescription(),
            court.getIsActive(),
            court.getVenues().getId(),
            court.getVenues().getName());

        // VALIDATION 2: Kiểm tra court có đang hoạt động không
        if (!court.getIsActive()) {
            log.warn("Attempted to book inactive court: id={}, description='{}'", court.getId(), court.getDescription());
            throw new IllegalStateException(
                String.format("Sân #%d (%s) đang tạm ngưng hoạt động. Vui lòng chọn sân khác.",
                    court.getId(), court.getDescription())
            );
        }

        // VALIDATION 3: Kiểm tra court có thuộc venue không
        if (!court.getVenues().getId().equals(bookingRequest.getVenueId())) {
            throw new IllegalArgumentException(
                String.format("Sân #%d không thuộc Venue #%d. Sân này thuộc Venue #%d (%s)",
                    bookingRequest.getCourtId(),
                    bookingRequest.getVenueId(),
                    court.getVenues().getId(),
                    court.getVenues().getName()
                )
            );
        }

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

        // Kiểm tra sân đã bị đặt chưa (check trong BookingItem thay vì Booking)
        // FIX: Truyền court.getId() để đảm bảo chỉ check đúng sân này
        boolean hasConflict = bookingItemRepository.existsConflictingBooking(
                court.getId(), bookingRequest.getStartTime(), bookingRequest.getEndTime());
        if (hasConflict) {
            throw new IllegalStateException("Sân đã được đặt trong khung giờ này. Vui lòng chọn khung giờ khác.");
        }

        // Tính giá tiền theo PriceService
        double totalPrice = priceService.calculateTotalCost(
                court.getVenues().getId(),
                bookingRequest.getStartTime(),
                bookingRequest.getEndTime()
        ).orElse(100000.0 * Duration.between(bookingRequest.getStartTime(), bookingRequest.getEndTime()).toHours());

        // Tạo booking mới với status PENDING_PAYMENT
        Booking booking = new Booking();
        booking.setUser(user);
        // XÓA setTotalPrice - totalPrice sẽ được tính động từ BookingItems
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setExpireTime(LocalDateTime.now().plusMinutes(PAYMENT_EXPIRE_MINUTES));
        booking.setPaymentProofUploaded(false);

        Booking savedBooking = bookingRepository.save(booking);

        // Tạo BookingItem
        BookingItem bookingItem = new BookingItem();
        bookingItem.setBooking(savedBooking);
        bookingItem.setCourt(court);
        bookingItem.setStartTime(bookingRequest.getStartTime());
        bookingItem.setEndTime(bookingRequest.getEndTime());
        bookingItem.setPrice(totalPrice);
        bookingItemRepository.save(bookingItem);

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
        List<BookingItem> items = bookingItemRepository.findByBookingId(savedBooking.getId());
        if (!items.isEmpty()) {
            User owner = items.get(0).getCourt().getVenues().getOwner();
            notificationService.createNotification(
                    owner,
                    currentUser,
                    booking,
                    NotificationType.PAYMENT_UPLOADED,
                    "Có khách đã chuyển khoản",
                    String.format("Khách hàng %s đã chuyển khoản cho đơn đặt sân #%d tại %s. Vui lòng kiểm tra và xác nhận.",
                            currentUser.getFullname(), booking.getId(), items.get(0).getCourt().getVenues().getName())
            );
        }

        return mapToBookingResponse(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponse acceptBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        User currentUser = getCurrentUser();
        List<BookingItem> items = bookingItemRepository.findByBookingId(bookingId);
        if (items.isEmpty()) {
            throw new ResourceNotFoundException("No booking items found");
        }
        User owner = items.get(0).getCourt().getVenues().getOwner();

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
        List<BookingItem> items = bookingItemRepository.findByBookingId(bookingId);
        if (items.isEmpty()) {
            throw new ResourceNotFoundException("No booking items found");
        }
        User owner = items.get(0).getCourt().getVenues().getOwner();

        if (!owner.getId().equals(currentUser.getId())) {
            throw new SecurityException("You don't have permission to reject this booking");
        }

        if (booking.getStatus() != BookingStatus.PAYMENT_UPLOADED) {
            throw new IllegalStateException("Booking must be in PAYMENT_UPLOADED status to reject");
        }

        // Từ chối đặt sân
        booking.setStatus(BookingStatus.REJECTED);
        booking.setRejectionReason(request.getRejectionReason());

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
    public List<BookingResponse> getAllBookingsForOwner() {
        User currentUser = getCurrentUser();
        // Lấy tất cả booking từ tất cả venues thuộc sở hữu của owner
        List<Booking> allBookings = bookingRepository.findAllBookingsForOwner(currentUser.getId());
        return allBookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)  // ✅ Thêm @Transactional để đảm bảo lazy loading hoạt động
    public BookingResponse getBookingById(Long id) {
        // ✅ Sử dụng method mới để load bookingItems cùng lúc
        Booking booking = bookingRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // ✅ Đảm bảo bookingItems được load
        if (booking.getBookingItems() == null || booking.getBookingItems().isEmpty()) {
            log.error("❌ CRITICAL: Booking {} has no items!", id);
            throw new IllegalStateException("Booking has no items");
        }
        log.info("✅ Loaded booking {} with {} items", id, booking.getBookingItems().size());
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
        // ✅ Ưu tiên lấy từ booking entity (đã được fetch)
        List<BookingItem> items = booking.getBookingItems();

        // ✅ Fallback: Nếu vẫn null hoặc empty, query lại
        if (items == null || items.isEmpty()) {
            log.warn("⚠️ BookingItems not loaded from entity, querying again for booking {}", booking.getId());
            items = bookingItemRepository.findByBookingId(booking.getId());
        }

        // ✅ Kiểm tra lại
        if (items == null || items.isEmpty()) {
            log.error("❌ CRITICAL: Booking {} has no items after query!", booking.getId());
            throw new IllegalStateException(
                    String.format("Booking %d has no items. This should not happen!", booking.getId())
            );
        }

        // Tính totalPrice động từ BookingItems
        double totalPrice = items.stream()
                .mapToDouble(BookingItem::getPrice)
                .sum();

        BookingItem firstItem = items.get(0);

        log.info("📋 Mapping Booking {}: courtId={}, startTime={}, endTime={}",
                booking.getId(),
                firstItem.getCourt().getId(),
                firstItem.getStartTime(),
                firstItem.getEndTime());

        // ✅ LUÔN set startTime và endTime từ firstItem
        BookingResponse.BookingResponseBuilder builder = BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .userName(booking.getUser().getFullname())
                .courtId(firstItem.getCourt().getId())
                .courtName(firstItem.getCourt().getDescription())  // ✅ Sửa: dùng description
                .venuesName(firstItem.getCourt().getVenues().getName())
                .startTime(firstItem.getStartTime())  // ✅ LUÔN set
                .endTime(firstItem.getEndTime())  // ✅ LUÔN set
                .totalPrice(totalPrice)
                .status(booking.getStatus())
                .expireTime(booking.getExpireTime())
                .paymentProofUploaded(booking.getPaymentProofUploaded())
                .paymentProofUrl(booking.getPaymentProofUrl())
                .paymentProofUploadedAt(booking.getPaymentProofUploadedAt())
                .rejectionReason(booking.getRejectionReason());

        // ✅ Thêm thông tin tài khoản chủ sân nếu status là PENDING_PAYMENT
        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
            User owner = firstItem.getCourt().getVenues().getOwner();
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
