package com.codewithvy.quanlydatsan.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Bật tính năng JPA Auditing cho ứng dụng.
 * Khi bật, các annotation như @CreatedDate, @LastModifiedDate (thường đặt trong BaseEntity)
 * sẽ tự động set giá trị thời gian tạo/cập nhật cho entity.
 * Lưu ý: chỉ phát huy tác dụng nếu entity có gắn AuditingEntityListener và các field tương ứng.
 */
@Configuration
@EnableJpaAuditing
public class AuditingConfig {
}
