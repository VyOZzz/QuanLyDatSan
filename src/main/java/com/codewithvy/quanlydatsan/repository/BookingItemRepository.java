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
     * TỐI ƯU: Check THỜI GIAN THỰC - tự động mở khóa NGAY khi hết giờ booking!
     * (chỉ check các booking đang active: PENDING, PAYMENT_UPLOADED, CONFIRMED)
     * VÀ chỉ tính booking CHƯA KẾT THÚC (endTime > NOW)
     *
     * FIX: So sánh theo court.id thay vì Court object để đảm bảo chỉ check đúng sân
     */
    @Query("SELECT COUNT(bi) > 0 FROM BookingItem bi " +
           "WHERE bi.court.id = :courtId " +  // ← FIX: So sánh theo ID, không phải object
           "AND bi.booking.status IN ('PENDING_PAYMENT', 'PAYMENT_UPLOADED', 'CONFIRMED') " +
           "AND bi.endTime > CURRENT_TIMESTAMP " +
           "AND ((bi.startTime < :endTime AND bi.endTime > :startTime))")
    boolean existsConflictingBooking(
        @Param("courtId") Long courtId,  // ← FIX: Đổi từ Court object sang Long courtId
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * Lấy tất cả slot đã đặt của một sân trong một khoảng thời gian
     * TỐI ƯU: Check THỜI GIAN THỰC - tự động mở khóa NGAY khi hết giờ booking!
     * Chỉ trả về các slot CHƯA KẾT THÚC (endTime > NOW)
     */
    @Query("SELECT bi FROM BookingItem bi " +
           "WHERE bi.court.id = :courtId " +
           "AND bi.booking.status IN ('PENDING_PAYMENT', 'PAYMENT_UPLOADED', 'CONFIRMED') " +
           "AND bi.endTime > CURRENT_TIMESTAMP " +  // ← TỐI ƯU: Bỏ qua booking đã hết giờ!
           "AND bi.startTime < :endTime AND bi.endTime > :startTime " +
           "ORDER BY bi.startTime")
    List<BookingItem> findBookedSlots(
        @Param("courtId") Long courtId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}

