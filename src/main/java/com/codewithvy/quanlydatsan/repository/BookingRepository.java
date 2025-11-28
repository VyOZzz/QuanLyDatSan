package com.codewithvy.quanlydatsan.repository;

import com.codewithvy.quanlydatsan.entity.Booking;
import com.codewithvy.quanlydatsan.entity.BookingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"bookingItems", "bookingItems.court", "bookingItems.court.venues", "bookingItems.court.venues.address", "user"})
    List<Booking> findByUserId(Long userId);

    // Tìm các booking hết hạn cần auto-cancel
    @EntityGraph(attributePaths = {"bookingItems", "bookingItems.court"})
    List<Booking> findByStatusAndExpireTimeBefore(BookingStatus status, LocalDateTime expireTime);

    // Tìm các booking đã kết thúc cần chuyển sang COMPLETED (query thông qua BookingItem)
    @EntityGraph(attributePaths = {"bookingItems", "bookingItems.court"})
    @Query("SELECT DISTINCT b FROM Booking b JOIN b.bookingItems bi WHERE b.status = :status AND bi.endTime < :endTime")
    List<Booking> findByStatusAndEndTimeBefore(@Param("status") BookingStatus status, @Param("endTime") LocalDateTime endTime);

    // Tìm các booking của một venues (cho chủ sân xem) - query thông qua BookingItem
    @EntityGraph(attributePaths = {"bookingItems", "bookingItems.court", "bookingItems.court.venues", "bookingItems.court.venues.address", "user"})
    @Query("SELECT DISTINCT b FROM Booking b JOIN b.bookingItems bi WHERE bi.court.venues.id = :venueId ORDER BY b.id DESC")
    List<Booking> findByVenueId(@Param("venueId") Long venueId);

    // Tìm các booking cần chủ sân xác nhận - query thông qua BookingItem
    @EntityGraph(attributePaths = {"bookingItems", "bookingItems.court", "bookingItems.court.venues", "bookingItems.court.venues.address", "user"})
    @Query("SELECT DISTINCT b FROM Booking b JOIN b.bookingItems bi WHERE bi.court.venues.owner.id = :ownerId AND b.status = 'PAYMENT_UPLOADED' ORDER BY b.paymentProofUploadedAt DESC")
    List<Booking> findPendingBookingsForOwner(@Param("ownerId") Long ownerId);

    // Tìm các booking cần chủ sân xác nhận theo venue cụ thể
    @EntityGraph(attributePaths = {"bookingItems", "bookingItems.court", "bookingItems.court.venues", "bookingItems.court.venues.address", "user"})
    @Query("SELECT DISTINCT b FROM Booking b JOIN b.bookingItems bi WHERE bi.court.venues.id = :venueId AND b.status = 'PAYMENT_UPLOADED' ORDER BY b.paymentProofUploadedAt DESC")
    List<Booking> findPendingBookingsByVenue(@Param("venueId") Long venueId);

    // Tìm TẤT CẢ booking của tất cả venues thuộc sở hữu của owner
    @EntityGraph(attributePaths = {"bookingItems", "bookingItems.court", "bookingItems.court.venues", "bookingItems.court.venues.address", "user"})
    @Query("SELECT DISTINCT b FROM Booking b JOIN b.bookingItems bi WHERE bi.court.venues.owner.id = :ownerId ORDER BY b.id DESC")
    List<Booking> findAllBookingsForOwner(@Param("ownerId") Long ownerId);

    // ✅ Lấy các booking đã được chấp nhận của user (cho tính năng lịch check-in sắp tới)
    // Chỉ lấy booking chưa kết thúc (endTime >= now)
    // FIX: Không dùng ORDER BY bi.startTime khi có DISTINCT - thay vào đó order theo booking.id
    @EntityGraph(attributePaths = {"bookingItems", "bookingItems.court", "bookingItems.court.venues", "bookingItems.court.venues.address"})
    @Query("SELECT DISTINCT b FROM Booking b JOIN b.bookingItems bi " +
           "WHERE b.user.id = :userId " +
           "AND b.status = 'CONFIRMED' " +
           "AND bi.endTime >= :now " +
           "ORDER BY b.id DESC")
    List<Booking> findConfirmedBookingsByUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // ✅ Lấy các booking upcoming (CONFIRMED) theo venue cho chủ sân
    // Lấy booking chưa kết thúc (endTime >= now)
    // FIX: Không dùng ORDER BY bi.startTime khi có DISTINCT - thay vào đó order theo booking.id
    @EntityGraph(attributePaths = {"bookingItems", "bookingItems.court", "bookingItems.court.venues", "bookingItems.court.venues.address", "user"})
    @Query("SELECT DISTINCT b FROM Booking b JOIN b.bookingItems bi " +
           "WHERE bi.court.venues.id = :venueId " +
           "AND b.status = 'CONFIRMED' " +
           "AND bi.endTime >= :now " +
           "ORDER BY b.id DESC")
    List<Booking> findUpcomingBookingsByVenue(@Param("venueId") Long venueId, @Param("now") LocalDateTime now);

    // ✅ SỬA: Thêm method này để load bookingItems cùng lúc
    @EntityGraph(attributePaths = {
            "user",  // ✅ Load user info
            "bookingItems",  // ✅ Load bookingItems
            "bookingItems.court",  // ✅ Load court
            "bookingItems.court.venues",  // ✅ SỬA: venues (số nhiều) không phải venue
            "bookingItems.court.venues.owner"  // ✅ Load owner (để lấy bank info)
    })
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdWithItems(@Param("id") Long id);
}
