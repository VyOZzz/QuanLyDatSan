package com.codewithvy.quanlydatsan.repository;

import com.codewithvy.quanlydatsan.entity.Venues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository cho Venues: cung cấp CRUD mặc định từ JpaRepository.
 */
@Repository
public interface VenuesRepository extends JpaRepository<Venues, Long> {
    /**
     * Tìm kiếm unified - tìm trong tất cả các trường (name, province, district, detail)
     * Sử dụng cho ô tìm kiếm duy nhất
     */
    @Query("SELECT DISTINCT v FROM Venues v " +
           "WHERE (:query IS NULL OR :query = '' OR " +
           "LOWER(v.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.address.provinceOrCity) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.address.district) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.address.detailAddress) LIKE LOWER(CONCAT('%', :query, '%'))) ")
    List<Venues> searchUnified(@Param("query") String query);

    /**
     * Tìm tất cả venues thuộc sở hữu của một owner cụ thể
     */
    List<Venues> findByOwnerId(Long ownerId);
}
