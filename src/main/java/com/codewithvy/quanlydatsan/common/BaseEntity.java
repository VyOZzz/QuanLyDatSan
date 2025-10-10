package com.codewithvy.quanlydatsan.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Lớp cơ sở cung cấp các cột audit thời gian cho entity kế thừa:
 * - createdAt: thời điểm tạo bản ghi (tự set khi persist lần đầu)
 * - updatedAt: thời điểm cập nhật gần nhất (tự set mỗi lần update)
 * Cần bật JPA Auditing (xem AuditingConfig) và gắn EntityListeners.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt; // thời điểm tạo bản ghi

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt; // thời điểm cập nhật gần nhất
}
