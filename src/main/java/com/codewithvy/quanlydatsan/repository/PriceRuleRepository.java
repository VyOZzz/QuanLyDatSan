package com.codewithvy.quanlydatsan.repository;

import com.codewithvy.quanlydatsan.entity.PriceRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.Optional;

public interface PriceRuleRepository extends JpaRepository<PriceRules, Long>{

    /**
     * Tìm quy tắc giá áp dụng cho một thời điểm cụ thể tại một sân.
     * Lưu ý: Xử lý trường hợp khung giờ qua đêm (ví dụ: 22:00 - 05:00).
     */
    @Query("SELECT pr FROM PriceRules pr WHERE pr.venues.id = :venueId AND " +
            "((pr.startTime <= pr.endTime AND :time >= pr.startTime AND :time < pr.endTime) OR " +
            "(pr.startTime > pr.endTime AND (:time >= pr.startTime OR :time < pr.endTime)))")
    Optional<PriceRules> findApplicablePriceRule(@Param("venueId") Long venueId, @Param("time") LocalTime time);
}
