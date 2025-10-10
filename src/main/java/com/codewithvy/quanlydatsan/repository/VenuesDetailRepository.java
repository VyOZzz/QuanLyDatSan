package com.codewithvy.quanlydatsan.repository;

import com.codewithvy.quanlydatsan.entity.VenuesDetail;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository cho VenuesDetail: kế thừa CRUD mặc định từ JpaRepository.
 */
public interface VenuesDetailRepository extends JpaRepository<VenuesDetail, Long> {
}
