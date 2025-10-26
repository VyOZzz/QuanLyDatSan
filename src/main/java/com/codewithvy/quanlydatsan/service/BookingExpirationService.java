package com.codewithvy.quanlydatsan.service;

import com.codewithvy.quanlydatsan.entity.Booking;
import com.codewithvy.quanlydatsan.entity.BookingStatus;
import com.codewithvy.quanlydatsan.entity.Court;
import com.codewithvy.quanlydatsan.entity.NotificationType;
import com.codewithvy.quanlydatsan.repository.BookingRepository;
import com.codewithvy.quanlydatsan.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service tự động xử lý các booking hết hạn
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingExpirationService {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final NotificationService notificationService;

    /**
     * Chạy mỗi 1 phút để kiểm tra và cancel các booking hết hạn
     */
    @Scheduled(fixedRate = 60000) // 60000ms = 1 phút
    @Transactional
    public void cancelExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();

        // Tìm các booking PENDING_PAYMENT đã hết hạn
        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpireTimeBefore(
                BookingStatus.PENDING_PAYMENT,
                now
        );

        if (!expiredBookings.isEmpty()) {
            log.info("Found {} expired bookings to cancel", expiredBookings.size());

            for (Booking booking : expiredBookings) {
                try {
                    // Đổi status sang EXPIRED
                    booking.setStatus(BookingStatus.EXPIRED);

                    // GIẢI PHÓNG SÂN - cho phép người khác đặt
                    Court court = booking.getCourt();
                    court.setBooked(false);
                    courtRepository.save(court);

                    bookingRepository.save(booking);

                    // Gửi thông báo cho người dùng
                    notificationService.createNotification(
                            booking.getUser(),
                            null, // Hệ thống gửi
                            booking,
                            NotificationType.BOOKING_EXPIRED,
                            "Đơn đặt sân hết hạn",
                            String.format("Đơn đặt sân #%d đã hết hạn thanh toán. Vui lòng tạo đơn mới nếu bạn vẫn muốn đặt sân.",
                                    booking.getId())
                    );

                    log.info("Cancelled expired booking ID: {}", booking.getId());
                } catch (Exception e) {
                    log.error("Error cancelling expired booking ID: {}", booking.getId(), e);
                }
            }
        }
    }

    /**
     * Chạy mỗi 5 phút để kiểm tra và hoàn thành các booking đã kết thúc
     */
    @Scheduled(fixedRate = 300000) // 300000ms = 5 phút
    @Transactional
    public void completeFinishedBookings() {
        LocalDateTime now = LocalDateTime.now();

        // Tìm các booking CONFIRMED đã kết thúc (endTime đã qua)
        List<Booking> finishedBookings = bookingRepository.findByStatusAndEndTimeBefore(
                BookingStatus.CONFIRMED,
                now
        );

        if (!finishedBookings.isEmpty()) {
            log.info("Found {} finished bookings to complete", finishedBookings.size());

            for (Booking booking : finishedBookings) {
                try {
                    // Đổi status sang COMPLETED
                    booking.setStatus(BookingStatus.COMPLETED);

                    // GIẢI PHÓNG SÂN - cho phép người khác đặt
                    Court court = booking.getCourt();
                    court.setBooked(false);
                    courtRepository.save(court);

                    bookingRepository.save(booking);

                    log.info("Completed booking ID: {}", booking.getId());
                } catch (Exception e) {
                    log.error("Error completing booking ID: {}", booking.getId(), e);
                }
            }
        }
    }
}
