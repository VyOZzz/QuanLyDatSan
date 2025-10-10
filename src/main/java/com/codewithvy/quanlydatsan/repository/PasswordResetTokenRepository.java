package com.codewithvy.quanlydatsan.repository;

import com.codewithvy.quanlydatsan.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository cho PasswordResetToken: tìm theo token và dọn các token hết hạn.
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    /** Tìm token theo chuỗi token. */
    Optional<PasswordResetToken> findByToken(String token);
    /** Xoá các token có expiryDate trước thời điểm cutoff (dọn rác định kỳ). */
    void deleteByExpiryDateBefore(Instant cutoff);
}
