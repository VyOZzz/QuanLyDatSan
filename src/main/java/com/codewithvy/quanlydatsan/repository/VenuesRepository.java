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
     * Tìm kiếm linh hoạt theo tên và/hoặc các trường địa chỉ (chuỗi chứa, không phân biệt hoa thường).
     * Nếu tham số là null thì bỏ qua điều kiện tương ứng.
     */
    @Query("SELECT v FROM Venues v " +
           "WHERE (:name IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:province IS NULL OR LOWER(v.address.provinceOrCity) LIKE LOWER(CONCAT('%', :province, '%'))) " +
           "AND (:district IS NULL OR LOWER(v.address.district) LIKE LOWER(CONCAT('%', :district, '%'))) " +
           "AND (:detail IS NULL OR LOWER(v.address.detailAddress) LIKE LOWER(CONCAT('%', :detail, '%'))) ")
    List<Venues> search(@Param("name") String name,
                        @Param("province") String province,
                        @Param("district") String district,
                        @Param("detail") String detail);
}
