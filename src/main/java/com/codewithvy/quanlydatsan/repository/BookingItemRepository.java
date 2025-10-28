package com.codewithvy.quanlydatsan.repository;

import com.codewithvy.quanlydatsan.entity.BookingItem;
import com.codewithvy.quanlydatsan.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {

    /**
     * Tìm tất cả booking items của một booking
     */
    List<BookingItem> findByBookingId(Long bookingId);

    /**
     * Kiểm tra xem một sân có bị trùng lịch trong khoảng thời gian không
     * (chỉ check các booking đang active: PENDING, PAYMENT_UPLOADED, CONFIRMED)
     */
    @Query("SELECT COUNT(bi) > 0 FROM BookingItem bi " +
           "WHERE bi.court = :court " +
           "AND bi.booking.status IN ('PENDING', 'PAYMENT_UPLOADED', 'CONFIRMED') " +
           "AND ((bi.startTime < :endTime AND bi.endTime > :startTime))")
    boolean existsConflictingBooking(
        @Param("court") Court court,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * Lấy tất cả slot đã đặt của một sân trong một khoảng thời gian
     */
    @Query("SELECT bi FROM BookingItem bi " +
           "WHERE bi.court.id = :courtId " +
           "AND bi.booking.status IN ('PENDING', 'PAYMENT_UPLOADED', 'CONFIRMED') " +
           "AND bi.startTime < :endTime AND bi.endTime > :startTime " +
           "ORDER BY bi.startTime")
    List<BookingItem> findBookedSlots(
        @Param("courtId") Long courtId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}

