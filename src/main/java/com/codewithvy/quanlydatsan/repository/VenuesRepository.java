package com.codewithvy.quanlydatsan.repository;

import com.codewithvy.quanlydatsan.entity.Venues;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenuesRepository extends JpaRepository<Venues, Long> {
    // Filter by Address.provinceOrCity (contains, case-insensitive)
    @Query("select v from Venues v join v.address a where lower(a.provinceOrCity) like lower(concat('%', :provinceOrCity, '%'))")
    List<Venues> searchByProvinceOrCity(@Param("provinceOrCity") String provinceOrCity);

    // Eagerly fetch address and courts for detail page to avoid LazyInitializationException
    @EntityGraph(attributePaths = {"address", "courts"})
    Optional<Venues> findWithCourtsById(Long id);
}
