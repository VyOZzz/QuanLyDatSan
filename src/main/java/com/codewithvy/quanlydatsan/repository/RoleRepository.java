package com.codewithvy.quanlydatsan.repository;

import com.codewithvy.quanlydatsan.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository cho Role: CRUD mặc định và truy vấn tìm theo tên role.
 */
public interface RoleRepository extends JpaRepository<Role, Integer> {
    /** Tìm role theo tên (ví dụ: ROLE_USER). */
    Optional<Role> findByName(String name);
}
