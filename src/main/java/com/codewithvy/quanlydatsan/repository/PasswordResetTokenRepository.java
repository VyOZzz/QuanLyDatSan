package com.codewithvy.quanlydatsan.repository;

import com.codewithvy.quanlydatsan.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByExpiryDateBefore(Instant cutoff);
}

