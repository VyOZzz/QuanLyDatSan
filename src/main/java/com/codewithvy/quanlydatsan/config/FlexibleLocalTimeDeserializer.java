package com.codewithvy.quanlydatsan.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Custom deserializer cho LocalTime hỗ trợ nhiều định dạng:
 * - "5" hoặc "05" → "05:00:00"
 * - "5:30" hoặc "05:30" → "05:30:00"
 * - "05:30:00" → "05:30:00"
 */
public class FlexibleLocalTimeDeserializer extends JsonDeserializer<LocalTime> {

    private static final DateTimeFormatter TIME_FORMATTER_FULL = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER_SHORT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String timeString = p.getText().trim();

        if (timeString.isEmpty()) {
            return null;
        }

        try {
            // Trường hợp 1: Chỉ có số giờ (ví dụ: "5", "05", "23")
            if (!timeString.contains(":")) {
                int hour = Integer.parseInt(timeString);
                if (hour < 0 || hour > 23) {
                    throw new IllegalArgumentException("Giờ phải từ 0-23");
                }
                return LocalTime.of(hour, 0, 0);
            }

            // Trường hợp 2: Có định dạng HH:mm:ss
            if (timeString.split(":").length == 3) {
                return LocalTime.parse(timeString, TIME_FORMATTER_FULL);
            }

            // Trường hợp 3: Có định dạng HH:mm hoặc H:mm
            if (timeString.split(":").length == 2) {
                return LocalTime.parse(timeString, TIME_FORMATTER_SHORT);
            }

            throw new IllegalArgumentException("Định dạng thời gian không hợp lệ: " + timeString);

        } catch (DateTimeParseException | NumberFormatException e) {
            throw new IllegalArgumentException(
                "Định dạng thời gian không hợp lệ: '" + timeString + "'. " +
                "Vui lòng sử dụng định dạng: 'H' (ví dụ: 5), 'HH:mm' (ví dụ: 05:30), hoặc 'HH:mm:ss' (ví dụ: 05:30:00)",
                e
            );
        }
    }
}

