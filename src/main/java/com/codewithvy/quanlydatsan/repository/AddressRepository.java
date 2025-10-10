package com.codewithvy.quanlydatsan.repository;

import com.codewithvy.quanlydatsan.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho entity Address: cung cấp CRUD mặc định từ JpaRepository.
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}
