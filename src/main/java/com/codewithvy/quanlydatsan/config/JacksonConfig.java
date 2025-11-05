package com.codewithvy.quanlydatsan.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Cấu hình Jackson để sử dụng múi giờ Việt Nam
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Thêm module hỗ trợ Java 8 Date/Time API
        mapper.registerModule(new JavaTimeModule());

        // Đặt múi giờ mặc định là Việt Nam (UTC+7)
        mapper.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Asia/Ho_Chi_Minh")));

        return mapper;
    }
}
