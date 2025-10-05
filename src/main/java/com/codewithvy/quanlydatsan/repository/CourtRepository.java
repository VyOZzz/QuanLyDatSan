package com.codewithvy.quanlydatsan.repository;

import com.codewithvy.quanlydatsan.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
}
