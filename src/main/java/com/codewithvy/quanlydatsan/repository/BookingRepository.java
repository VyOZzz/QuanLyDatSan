package com.codewithvy.quanlydatsan.repository;

import com.codewithvy.quanlydatsan.entity.Booking;
import com.codewithvy.quanlydatsan.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.court.id = :courtId AND b.status NOT IN ('CANCELLED', 'REJECTED', 'EXPIRED', 'COMPLETED') AND " +
           "((b.startTime < :endTime AND b.endTime > :startTime))")
    List<Booking> findOverlappingBookings(@Param("courtId") Long courtId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    List<Booking> findByUserId(Long userId);

    // Tìm các booking hết hạn cần auto-cancel
    List<Booking> findByStatusAndExpireTimeBefore(BookingStatus status, LocalDateTime expireTime);

    // Tìm các booking đã kết thúc cần chuyển sang COMPLETED
    List<Booking> findByStatusAndEndTimeBefore(BookingStatus status, LocalDateTime endTime);

    // Tìm các booking của một venues (cho chủ sân xem)
    @Query("SELECT b FROM Booking b WHERE b.court.venues.id = :venueId ORDER BY b.startTime DESC")
    List<Booking> findByVenueId(@Param("venueId") Long venueId);

    // Tìm các booking cần chủ sân xác nhận
    @Query("SELECT b FROM Booking b WHERE b.court.venues.owner.id = :ownerId AND b.status = 'PAYMENT_UPLOADED' ORDER BY b.paymentProofUploadedAt DESC")
    List<Booking> findPendingBookingsForOwner(@Param("ownerId") Long ownerId);
}
