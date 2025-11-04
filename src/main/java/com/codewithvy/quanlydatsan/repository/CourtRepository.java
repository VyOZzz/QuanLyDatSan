package com.codewithvy.quanlydatsan.repository;

import com.codewithvy.quanlydatsan.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho Court: CRUD mặc định từ JpaRepository.
 */
@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
    /**
     * Tìm tất cả courts thuộc một venue
     */
    List<Court> findByVenuesId(Long venueId);
}
