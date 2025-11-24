package com.codewithvy.quanlydatsan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AnalyticsDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Dữ liệu phân tích tổng hợp cho chủ sân")
    public static class AnalyticsData {
        @Schema(description = "Khoảng thời gian", example = "MONTH")
        private String period;

        @Schema(description = "Ngày bắt đầu", example = "2024-11-01T00:00:00Z")
        private LocalDateTime startDate;

        @Schema(description = "Ngày kết thúc", example = "2024-11-30T23:59:59Z")
        private LocalDateTime endDate;

        @Schema(description = "Tổng quan doanh thu")
        private RevenueOverview overview;

        @Schema(description = "Doanh thu theo ngày")
        private List<RevenueByDate> revenueByDate;

        @Schema(description = "Doanh thu theo tuần (5 tuần gần nhất)")
        private List<RevenueByWeek> revenueByWeek;

        @Schema(description = "Doanh thu theo tháng (12 tháng)")
        private List<RevenueByMonth> revenueByMonth;

        @Schema(description = "Hiệu suất các sân")
        private List<VenuePerformance> venuePerformance;

        @Schema(description = "Top khách hàng")
        private List<TopCustomer> topCustomers;

        @Schema(description = "Thống kê theo khung giờ")
        private List<TimeSlotStats> timeSlotStats;

        @Schema(description = "Thống kê phương thức thanh toán")
        private List<PaymentMethodStats> paymentMethodStats;

        @Schema(description = "Phân tích tự động")
        private Insights insights;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Tổng quan doanh thu")
    public static class RevenueOverview {
        @Schema(description = "Tổng doanh thu", example = "50000000")
        private BigDecimal totalRevenue;

        @Schema(description = "Tổng số booking", example = "150")
        private Long totalBookings;

        @Schema(description = "Giá trị trung bình mỗi booking", example = "333333")
        private BigDecimal averageBookingValue;

        @Schema(description = "Thống kê trạng thái booking")
        private BookingStats bookingStats;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thống kê theo trạng thái booking")
    public static class BookingStats {
        @Schema(description = "Tổng số booking", example = "156")
        private Long totalBookings;

        @Schema(description = "Số booking đang chờ thanh toán", example = "5")
        private Long pendingCount;

        @Schema(description = "Số booking đã xác nhận", example = "100")
        private Long confirmedCount;

        @Schema(description = "Số booking đã hoàn thành", example = "120")
        private Long completedCount;

        @Schema(description = "Số booking bị từ chối", example = "3")
        private Long rejectedCount;

        @Schema(description = "Số booking đã hủy", example = "7")
        private Long cancelledCount;

        @Schema(description = "Tỷ lệ chuyển đổi (%)", example = "85.5")
        private Double conversionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Doanh thu theo ngày")
    public static class RevenueByDate {
        @Schema(description = "Ngày", example = "2025-11-24")
        private LocalDate date;

        @Schema(description = "Doanh thu", example = "500000")
        private BigDecimal revenue;

        @Schema(description = "Số booking", example = "3")
        private Long bookingCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Doanh thu theo tuần")
    public static class RevenueByWeek {
        @Schema(description = "Số tuần", example = "1")
        private Integer weekNumber;

        @Schema(description = "Label tuần", example = "Tuần 1")
        private String weekLabel;

        @Schema(description = "Ngày bắt đầu tuần", example = "2025-11-01")
        private LocalDate weekStart;

        @Schema(description = "Ngày kết thúc tuần", example = "2025-11-07")
        private LocalDate weekEnd;

        @Schema(description = "Range ngày hiển thị", example = "01/11 - 07/11")
        private String weekRange;

        @Schema(description = "Doanh thu", example = "2000000")
        private BigDecimal revenue;

        @Schema(description = "Số booking", example = "10")
        private Long bookingCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Doanh thu theo tháng")
    public static class RevenueByMonth {
        @Schema(description = "Năm", example = "2025")
        private Integer year;

        @Schema(description = "Tháng", example = "11")
        private Integer month;

        @Schema(description = "Tên tháng", example = "Nov")
        private String monthName;

        @Schema(description = "Doanh thu", example = "10000000")
        private BigDecimal revenue;

        @Schema(description = "Số booking", example = "50")
        private Long bookingCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Hiệu suất sân")
    public static class VenuePerformance {
        @Schema(description = "ID sân", example = "1")
        private Long id;

        @Schema(description = "Tên sân", example = "Sân bóng A")
        private String name;

        @Schema(description = "Số booking", example = "30")
        private Long bookingCount;

        @Schema(description = "Doanh thu", example = "8000000")
        private BigDecimal revenue;

        @Schema(description = "Số booking đã hoàn thành", example = "28")
        private Long completedBookings;

        @Schema(description = "Đánh giá trung bình", example = "4.5")
        private Double averageRating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Top khách hàng")
    public static class TopCustomer {
        @Schema(description = "ID người dùng", example = "5")
        private Long userId;

        @Schema(description = "Tên người dùng", example = "Nguyễn Văn A")
        private String userName;

        @Schema(description = "Số điện thoại", example = "0912345678")
        private String userPhone;

        @Schema(description = "Số booking", example = "15")
        private Long bookingCount;

        @Schema(description = "Tổng chi tiêu", example = "3000000")
        private BigDecimal totalSpent;

        @Schema(description = "Ngày đặt gần nhất", example = "2024-11-15")
        private LocalDate lastBookingDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thống kê theo khung giờ")
    public static class TimeSlotStats {
        @Schema(description = "Giờ trong ngày (0-23)", example = "18")
        private Integer hour;

        @Schema(description = "Label giờ hiển thị", example = "18:00")
        private String hourLabel;

        @Schema(description = "Số booking", example = "25")
        private Long bookingCount;

        @Schema(description = "Doanh thu", example = "5000000")
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thống kê phương thức thanh toán")
    public static class PaymentMethodStats {
        @Schema(description = "Phương thức thanh toán", example = "BANK_TRANSFER")
        private String method;

        @Schema(description = "Tên phương thức hiển thị", example = "Chuyển khoản")
        private String methodLabel;

        @Schema(description = "Số lượng giao dịch", example = "80")
        private Long count;

        @Schema(description = "Tổng số tiền", example = "40000000")
        private BigDecimal totalAmount;

        @Schema(description = "Tỷ lệ phần trăm", example = "80.0")
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Phân tích tự động (Insights)")
    public static class Insights {
        @Schema(description = "Giờ cao điểm", example = "18")
        private Integer peakHour;

        @Schema(description = "Label giờ cao điểm", example = "18:00")
        private String peakHourLabel;

        @Schema(description = "Số booking giờ cao điểm", example = "25")
        private Long peakHourBookings;

        @Schema(description = "Sân hoạt động tốt nhất")
        private BestVenue bestVenue;

        @Schema(description = "Ngày doanh thu cao nhất")
        private BestDay bestDay;

        @Schema(description = "Tỷ lệ tăng trưởng (%)", example = "15.5")
        private Double growthRate;

        @Schema(description = "Label tỷ lệ tăng trưởng", example = "+15.5%")
        private String growthRateLabel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Sân tốt nhất")
    public static class BestVenue {
        @Schema(description = "ID sân", example = "1")
        private Long venueId;

        @Schema(description = "Tên sân", example = "Sân Cầu Lông Hà Nội")
        private String venueName;

        @Schema(description = "Doanh thu", example = "20500000")
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Ngày tốt nhất")
    public static class BestDay {
        @Schema(description = "Ngày", example = "2024-11-15")
        private LocalDate date;

        @Schema(description = "Doanh thu", example = "2500000")
        private BigDecimal revenue;

        @Schema(description = "Số booking", example = "10")
        private Long bookingCount;
    }
}

