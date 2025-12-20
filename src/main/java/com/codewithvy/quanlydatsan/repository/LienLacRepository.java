package com.codewithvy.quanlydatsan.repository;

import com.codewithvy.quanlydatsan.entity.LienLac;
import com.codewithvy.quanlydatsan.entity.LienLacStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface LienLacRepository extends JpaRepository<LienLac, Long> {

    // Tìm tất cả liên lạc theo user
    List<LienLac> findByUserId(Long userId);

    // Tìm liên lạc theo venue
    List<LienLac> findByVenueId(Long venueId);

    // Tìm liên lạc theo trạng thái
    List<LienLac> findByStatus(LienLacStatus status);

    // Đếm liên lạc theo trạng thái
    long countByStatus(LienLacStatus status);

    // Đếm liên lạc theo venue và trạng thái
    long countByVenueIdAndStatus(Long venueId, LienLacStatus status);

    // Tìm liên lạc theo người xử lý
    List<LienLac> findByHandlerId(Long handlerId);

    // Đếm tổng số liên lạc theo khoảng thời gian
    @Query("SELECT COUNT(l) FROM LienLac l WHERE l.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    // Đếm liên lạc theo trạng thái và khoảng thời gian
    @Query("SELECT COUNT(l) FROM LienLac l WHERE l.status = :status AND l.createdAt BETWEEN :startDate AND :endDate")
    long countByStatusAndCreatedAtBetween(
            @Param("status") LienLacStatus status,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    // Đếm liên lạc theo venue và khoảng thời gian
    @Query("SELECT COUNT(l) FROM LienLac l WHERE l.venue.id = :venueId AND l.createdAt BETWEEN :startDate AND :endDate")
    long countByVenueIdAndCreatedAtBetween(
            @Param("venueId") Long venueId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    // Lấy liên lạc theo venue và owner của venue đó
    @Query("SELECT l FROM LienLac l WHERE l.venue.owner.id = :ownerId ORDER BY l.createdAt DESC")
    List<LienLac> findByVenueOwnerId(@Param("ownerId") Long ownerId);

    // Đếm liên lạc theo owner
    @Query("SELECT COUNT(l) FROM LienLac l WHERE l.venue.owner.id = :ownerId AND l.createdAt BETWEEN :startDate AND :endDate")
    long countByVenueOwnerIdAndCreatedAtBetween(
            @Param("ownerId") Long ownerId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    // Đếm liên lạc theo owner và trạng thái
    @Query("SELECT COUNT(l) FROM LienLac l WHERE l.venue.owner.id = :ownerId AND l.status = :status")
    long countByVenueOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") LienLacStatus status);
}
