package com.codewithvy.quanlydatsan.repository;

import com.codewithvy.quanlydatsan.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}
