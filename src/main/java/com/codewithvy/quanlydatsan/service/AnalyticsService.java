package com.codewithvy.quanlydatsan.service;

import com.codewithvy.quanlydatsan.dto.AnalyticsDTO.*;

import java.time.LocalDate;

public interface AnalyticsService {

    /**
     * Lấy dữ liệu phân tích tổng hợp theo period
     * @param ownerId ID chủ sân
     * @param period DAY|WEEK|MONTH|YEAR
     * @param startDate Ngày bắt đầu (optional, mặc định là đầu period)
     * @param endDate Ngày kết thúc (optional, mặc định là cuối period)
     * @return AnalyticsData đầy đủ
     */
    AnalyticsData getAnalytics(Long ownerId, String period, LocalDate startDate, LocalDate endDate);

    /**
     * Lấy dữ liệu phân tích theo venue cụ thể
     */
    AnalyticsData getVenueAnalytics(Long venueId, String period, LocalDate startDate, LocalDate endDate);
}

