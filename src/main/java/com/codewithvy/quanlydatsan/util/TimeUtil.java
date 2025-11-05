package com.codewithvy.quanlydatsan.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class để xử lý thời gian theo múi giờ Việt Nam
 */
public class TimeUtil {

    // Múi giờ Việt Nam
    public static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    // Formatter cho thời gian Việt Nam
    public static final DateTimeFormatter VIETNAM_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(VIETNAM_ZONE);

    /**
     * Chuyển LocalDateTime sang ZonedDateTime với múi giờ Việt Nam
     */
    public static ZonedDateTime toVietnamTime(LocalDateTime localDateTime) {
        return localDateTime.atZone(VIETNAM_ZONE);
    }

    /**
     * Format LocalDateTime theo múi giờ Việt Nam
     */
    public static String formatVietnamTime(LocalDateTime localDateTime) {
        return toVietnamTime(localDateTime).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Lấy thời gian hiện tại theo múi giờ Việt Nam
     */
    public static LocalDateTime nowVietnam() {
        return LocalDateTime.now(VIETNAM_ZONE);
    }

    /**
     * Lấy ZonedDateTime hiện tại theo múi giờ Việt Nam
     */
    public static ZonedDateTime nowVietnamZoned() {
        return ZonedDateTime.now(VIETNAM_ZONE);
    }
}
