package com.codewithvy.quanlydatsan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entity lưu token đặt lại mật khẩu tạm thời cho một người dùng, có thời hạn.
 */
@Entity
@Table(name = "password_reset_token", indexes = {
        @Index(name = "idx_password_reset_token_token", columnList = "token", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id bản ghi token

    @Column(nullable = false, unique = true, length = 100)
    private String token; // chuỗi token ngẫu nhiên (base64-url)

    @Column(nullable = false)
    private Instant expiryDate; // thời điểm hết hạn token

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // người dùng sở hữu token này
}
