package com.codewithvy.quanlydatsan.service.impl;

import com.codewithvy.quanlydatsan.dto.*;
import com.codewithvy.quanlydatsan.entity.*;
import com.codewithvy.quanlydatsan.exception.ResourceNotFoundException;
import com.codewithvy.quanlydatsan.exception.UnauthorizedException;
import com.codewithvy.quanlydatsan.repository.BookingRepository;
import com.codewithvy.quanlydatsan.repository.BookingItemRepository;
import com.codewithvy.quanlydatsan.repository.CourtRepository;
import com.codewithvy.quanlydatsan.repository.UserRepository;
import com.codewithvy.quanlydatsan.repository.VenuesRepository;
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
    private final VenuesRepository venuesRepository;
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

        // ========== KIỂM TRA CHẾ ĐỘ: LEGACY (1 sân) hay MULTI (nhiều sân) ==========
        List<BookingItemRequest> itemsToBook;

        if (bookingRequest.getBookingItems() != null && !bookingRequest.getBookingItems().isEmpty()) {
            // ✅ NEW MODE: Đặt nhiều sân
            log.info("Creating MULTI-COURT booking for user={}, venueId={}, {} courts",
                user.getEmail(), bookingRequest.getVenueId(), bookingRequest.getBookingItems().size());
            itemsToBook = bookingRequest.getBookingItems();
        } else {
            // ✅ LEGACY MODE: Đặt 1 sân (backward compatible)
            if (bookingRequest.getCourtId() == null || bookingRequest.getStartTime() == null || bookingRequest.getEndTime() == null) {
                throw new IllegalArgumentException("Cần cung cấp bookingItems hoặc (courtId + startTime + endTime)");
            }
            log.info("Creating SINGLE-COURT booking (legacy mode) for user={}, courtId={}, venueId={}",
                user.getEmail(), bookingRequest.getCourtId(), bookingRequest.getVenueId());

            // Convert legacy request thành BookingItemRequest
            BookingItemRequest legacyItem = new BookingItemRequest();
            legacyItem.setCourtId(bookingRequest.getCourtId());
            legacyItem.setStartTime(bookingRequest.getStartTime());
            legacyItem.setEndTime(bookingRequest.getEndTime());
            itemsToBook = List.of(legacyItem);
        }

        // ========== VALIDATION CHO TẤT CẢ CÁC SÂN ==========
        LocalDateTime now = LocalDateTime.now();

        for (BookingItemRequest item : itemsToBook) {
            // Validate thời gian
            if (item.getStartTime().isBefore(now)) {
                throw new IllegalArgumentException(
                    String.format("Thời gian bắt đầu của sân #%d phải sau thời điểm hiện tại", item.getCourtId())
                );
            }
            if (item.getEndTime().isBefore(item.getStartTime())) {
                throw new IllegalArgumentException(
                    String.format("Thời gian kết thúc của sân #%d phải sau thời gian bắt đầu", item.getCourtId())
                );
            }
            if (item.getEndTime().isBefore(now)) {
                throw new IllegalArgumentException(
                    String.format("Thời gian kết thúc của sân #%d phải sau thời điểm hiện tại", item.getCourtId())
                );
            }

            // Validate court tồn tại
            Court court = courtRepository.findById(item.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    String.format("Không tìm thấy sân với ID: %d", item.getCourtId())
                ));

            // Validate court đang hoạt động
            if (!court.getIsActive()) {
                throw new IllegalStateException(
                    String.format("Sân #%d (%s) đang tạm ngưng hoạt động. Vui lòng chọn sân khác.",
                        court.getId(), court.getDescription())
                );
            }

            // Validate court thuộc venue
            if (!court.getVenues().getId().equals(bookingRequest.getVenueId())) {
                throw new IllegalArgumentException(
                    String.format("Sân #%d không thuộc Venue #%d. Sân này thuộc Venue #%d (%s)",
                        item.getCourtId(), bookingRequest.getVenueId(),
                        court.getVenues().getId(), court.getVenues().getName())
                );
            }

            // Validate conflict (sân đã bị đặt chưa)
            boolean hasConflict = bookingItemRepository.existsConflictingBooking(
                court.getId(), item.getStartTime(), item.getEndTime()
            );
            if (hasConflict) {
                throw new IllegalStateException(
                    String.format("Sân #%d (%s) đã được đặt trong khung giờ %s - %s. Vui lòng chọn khung giờ khác.",
                        court.getId(), court.getDescription(), item.getStartTime(), item.getEndTime())
                );
            }
        }

        // ========== TẠO BOOKING ==========
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setExpireTime(LocalDateTime.now().plusMinutes(PAYMENT_EXPIRE_MINUTES));
        booking.setPaymentProofUploaded(false);

        Booking savedBooking = bookingRepository.save(booking);

        // ========== TẠO CÁC BOOKING ITEMS ==========
        for (BookingItemRequest itemRequest : itemsToBook) {
            Court court = courtRepository.findById(itemRequest.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court not found"));

            // Tính giá tiền cho từng item
            double itemPrice = priceService.calculateTotalCost(
                court.getVenues().getId(),
                itemRequest.getStartTime(),
                itemRequest.getEndTime()
            ).orElse(100000.0 * Duration.between(itemRequest.getStartTime(), itemRequest.getEndTime()).toHours());

            BookingItem bookingItem = new BookingItem();
            bookingItem.setBooking(savedBooking);
            bookingItem.setCourt(court);
            bookingItem.setStartTime(itemRequest.getStartTime());
            bookingItem.setEndTime(itemRequest.getEndTime());
            bookingItem.setPrice(itemPrice);
            bookingItemRepository.save(bookingItem);

            log.info("Created BookingItem: courtId={}, startTime={}, endTime={}, price={}",
                court.getId(), itemRequest.getStartTime(), itemRequest.getEndTime(), itemPrice);
        }

        log.info("✅ Successfully created booking #{} with {} court(s), totalPrice={}",
            savedBooking.getId(), itemsToBook.size(), savedBooking.getTotalPrice());

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
    public List<BookingResponse> getPendingBookingsByVenue(Long venueId) {
        User currentUser = getCurrentUser();
        // Kiểm tra xem venue có thuộc sở hữu của owner không
        Venues venue = venuesRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        if (!venue.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Bạn không có quyền truy cập venue này");
        }

        List<Booking> bookings = bookingRepository.findPendingBookingsByVenue(venueId);
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

        // ✅ Map bookingItems to BookingItemResponse
        List<BookingItemResponse> bookingItemResponses = items.stream()
                .map(item -> BookingItemResponse.builder()
                        .id(item.getId())
                        .courtId(item.getCourt().getId())
                        .courtName(item.getCourt().getDescription())
                        .startTime(item.getStartTime())
                        .endTime(item.getEndTime())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        // ✅ Map địa chỉ sân
        Address venueAddress = firstItem.getCourt().getVenues().getAddress();
        AddressDTO venueAddressDTO = null;
        if (venueAddress != null) {
            venueAddressDTO = AddressDTO.builder()
                    .id(venueAddress.getId())
                    .provinceOrCity(venueAddress.getProvinceOrCity())
                    .district(venueAddress.getDistrict())
                    .detailAddress(venueAddress.getDetailAddress())
                    .build();
        }

        // ✅ LUÔN set startTime và endTime từ firstItem (for backward compatibility)
        BookingResponse.BookingResponseBuilder builder = BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .userName(booking.getUser().getFullname())
                .courtId(firstItem.getCourt().getId())
                .courtName(firstItem.getCourt().getDescription())  // ✅ Sửa: dùng description
                .venuesName(firstItem.getCourt().getVenues().getName())
                .venueAddress(venueAddressDTO)  // ✅ Thêm địa chỉ sân
                .startTime(firstItem.getStartTime())  // ✅ LUÔN set (legacy field)
                .endTime(firstItem.getEndTime())  // ✅ LUÔN set (legacy field)
                .totalPrice(totalPrice)
                .status(booking.getStatus())
                .expireTime(booking.getExpireTime())
                .paymentProofUploaded(booking.getPaymentProofUploaded())
                .paymentProofUrl(booking.getPaymentProofUrl())
                .paymentProofUploadedAt(booking.getPaymentProofUploadedAt())
                .rejectionReason(booking.getRejectionReason())
                .bookingItems(bookingItemResponses);  // ✅ NEW: Add bookingItems array

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

    @Override
    public List<BookingResponse> getMyUpcomingBookings() {
        User currentUser = getCurrentUser();
        List<Booking> bookings = bookingRepository.findConfirmedBookingsByUser(currentUser.getId(), LocalDateTime.now());
        return bookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getUpcomingBookingsByVenue(Long venueId) {
        // Kiểm tra quyền sở hữu venue
        User currentUser = getCurrentUser();
        Venues venue = venuesRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + venueId));

        if (!venue.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Bạn không có quyền xem booking của sân này");
        }

        // Lấy các booking CONFIRMED và chưa kết thúc
        List<Booking> bookings = bookingRepository.findUpcomingBookingsByVenue(venueId, LocalDateTime.now());
        return bookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    private User getCurrentUser() {
        String phone = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
