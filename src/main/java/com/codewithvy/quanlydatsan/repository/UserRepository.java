package com.codewithvy.quanlydatsan.repository;

import com.codewithvy.quanlydatsan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository cho User: kế thừa CRUD mặc định và bổ sung truy vấn tìm/kiểm tra trùng.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /** Tìm user theo username. */
    Optional<User> findByUsername(String username);
    /** Kiểm tra username đã tồn tại chưa. */
    boolean existsByUsername(String username);
    /** Kiểm tra số điện thoại đã tồn tại chưa. */
    boolean existsByPhone(String phone);
    /** Tìm user theo email. */
    Optional<User> findByEmail(String email);
    /** Kiểm tra email đã tồn tại chưa. */
    boolean existsByEmail(String email);
}
