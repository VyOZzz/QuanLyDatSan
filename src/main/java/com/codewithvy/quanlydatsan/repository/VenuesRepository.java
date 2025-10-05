package com.codewithvy.quanlydatsan.repository;

import com.codewithvy.quanlydatsan.entity.Venues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VenuesRepository extends JpaRepository<Venues, Long> {
}

