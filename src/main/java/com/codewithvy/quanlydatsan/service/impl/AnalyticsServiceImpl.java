package com.codewithvy.quanlydatsan.service.impl;

import com.codewithvy.quanlydatsan.dto.AnalyticsDTO.*;
import com.codewithvy.quanlydatsan.entity.Booking;
import com.codewithvy.quanlydatsan.entity.BookingItem;
import com.codewithvy.quanlydatsan.entity.BookingStatus;
import com.codewithvy.quanlydatsan.exception.ResourceNotFoundException;
import com.codewithvy.quanlydatsan.repository.BookingRepository;
import com.codewithvy.quanlydatsan.repository.VenuesRepository;
import com.codewithvy.quanlydatsan.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final BookingRepository bookingRepository;
    private final VenuesRepository venuesRepository;

    @Override
    @Transactional(readOnly = true)
    public AnalyticsData getAnalytics(Long ownerId, String period, LocalDate startDate, LocalDate endDate) {
        // Xác định khoảng thời gian dựa trên period nếu không có startDate/endDate
        DateRange dateRange = calculateDateRange(period, startDate, endDate);
        LocalDate rangeStart = dateRange.start;
        LocalDate rangeEnd = dateRange.end;

        // Lấy tất cả booking của owner trong khoảng thời gian
        List<Booking> bookings = getOwnerBookingsInRange(ownerId, rangeStart, rangeEnd);

        // Tính toán các metrics
        RevenueOverview overview = calculateOverview(bookings);
        List<RevenueByDate> revenueByDate = calculateRevenueByDate(bookings, rangeStart, rangeEnd);
        List<RevenueByWeek> revenueByWeek = calculateRevenueByWeek(bookings, rangeStart, rangeEnd);
        List<RevenueByMonth> revenueByMonth = calculateRevenueByMonth(bookings, rangeStart, rangeEnd);
        List<VenuePerformance> venuePerformance = calculateVenuePerformance(bookings);
        List<TopCustomer> topCustomers = calculateTopCustomers(bookings, 10);
        List<TimeSlotStats> timeSlotStats = calculateTimeSlotStats(bookings);
        List<PaymentMethodStats> paymentMethodStats = calculatePaymentMethodStats(bookings);

        // Tính insights
        Insights insights = calculateInsights(timeSlotStats, venuePerformance, revenueByDate);

        return AnalyticsData.builder()
                .period(period)
                .startDate(rangeStart.atStartOfDay())
                .endDate(rangeEnd.atTime(LocalTime.MAX))
                .overview(overview)
                .revenueByDate(revenueByDate)
                .revenueByWeek(revenueByWeek)
                .revenueByMonth(revenueByMonth)
                .venuePerformance(venuePerformance)
                .topCustomers(topCustomers)
                .timeSlotStats(timeSlotStats)
                .paymentMethodStats(paymentMethodStats)
                .insights(insights)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsData getVenueAnalytics(Long venueId, String period, LocalDate startDate, LocalDate endDate) {
        // Kiểm tra venue tồn tại
        venuesRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân với ID: " + venueId));

        // Xác định khoảng thời gian
        DateRange dateRange = calculateDateRange(period, startDate, endDate);
        LocalDate rangeStart = dateRange.start;
        LocalDate rangeEnd = dateRange.end;

        // Lấy booking của venue
        List<Booking> allVenueBookings = bookingRepository.findByVenueId(venueId);
        List<Booking> bookings = filterBookingsByDateRange(allVenueBookings, rangeStart, rangeEnd);

        // Tính toán các metrics (tương tự như owner)
        RevenueOverview overview = calculateOverview(bookings);
        List<RevenueByDate> revenueByDate = calculateRevenueByDate(bookings, rangeStart, rangeEnd);
        List<RevenueByWeek> revenueByWeek = calculateRevenueByWeek(bookings, rangeStart, rangeEnd);
        List<RevenueByMonth> revenueByMonth = calculateRevenueByMonth(bookings, rangeStart, rangeEnd);
        List<TopCustomer> topCustomers = calculateTopCustomers(bookings, 10);
        List<TimeSlotStats> timeSlotStats = calculateTimeSlotStats(bookings);
        List<PaymentMethodStats> paymentMethodStats = calculatePaymentMethodStats(bookings);

        Insights insights = calculateInsights(timeSlotStats, Collections.emptyList(), revenueByDate);

        return AnalyticsData.builder()
                .period(period)
                .startDate(rangeStart.atStartOfDay())
                .endDate(rangeEnd.atTime(LocalTime.MAX))
                .overview(overview)
                .revenueByDate(revenueByDate)
                .revenueByWeek(revenueByWeek)
                .revenueByMonth(revenueByMonth)
                .venuePerformance(Collections.emptyList()) // Venue cụ thể không cần so sánh
                .topCustomers(topCustomers)
                .timeSlotStats(timeSlotStats)
                .paymentMethodStats(paymentMethodStats)
                .insights(insights)
                .build();
    }

    // ============= HELPER METHODS =============

    private static class DateRange {
        LocalDate start;
        LocalDate end;
        DateRange(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }
    }

    private DateRange calculateDateRange(String period, LocalDate startDate, LocalDate endDate) {
        LocalDate now = LocalDate.now();

        if (startDate != null && endDate != null) {
            return new DateRange(startDate, endDate);
        }

        switch (period.toUpperCase()) {
            case "DAY":
                return new DateRange(now, now);
            case "WEEK":
                LocalDate weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate weekEnd = weekStart.plusDays(6);
                return new DateRange(weekStart, weekEnd);
            case "MONTH":
                return new DateRange(
                    now.withDayOfMonth(1),
                    now.withDayOfMonth(now.lengthOfMonth())
                );
            case "YEAR":
                return new DateRange(
                    LocalDate.of(now.getYear(), 1, 1),
                    LocalDate.of(now.getYear(), 12, 31)
                );
            default:
                // Mặc định: 30 ngày gần nhất
                return new DateRange(now.minusDays(29), now);
        }
    }

    private List<Booking> getOwnerBookingsInRange(Long ownerId, LocalDate startDate, LocalDate endDate) {
        List<Booking> allBookings = bookingRepository.findAllBookingsForOwner(ownerId);
        return filterBookingsByDateRange(allBookings, startDate, endDate);
    }

    private List<Booking> filterBookingsByDateRange(List<Booking> bookings, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return bookings.stream()
                .filter(b -> !b.getBookingItems().isEmpty())
                .filter(b -> {
                    LocalDateTime bookingTime = b.getBookingItems().get(0).getStartTime();
                    return !bookingTime.isBefore(startDateTime) && !bookingTime.isAfter(endDateTime);
                })
                .collect(Collectors.toList());
    }

    private RevenueOverview calculateOverview(List<Booking> bookings) {
        long totalBookings = bookings.size();

        Map<BookingStatus, Long> statusCounts = bookings.stream()
                .collect(Collectors.groupingBy(Booking::getStatus, Collectors.counting()));

        long pendingCount = statusCounts.getOrDefault(BookingStatus.PENDING_PAYMENT, 0L)
                + statusCounts.getOrDefault(BookingStatus.PAYMENT_UPLOADED, 0L);
        long confirmedCount = statusCounts.getOrDefault(BookingStatus.CONFIRMED, 0L);
        long completedCount = statusCounts.getOrDefault(BookingStatus.COMPLETED, 0L);
        long rejectedCount = statusCounts.getOrDefault(BookingStatus.REJECTED, 0L);
        long cancelledCount = statusCounts.getOrDefault(BookingStatus.CANCELLED, 0L)
                + statusCounts.getOrDefault(BookingStatus.EXPIRED, 0L);

        // Tính doanh thu chỉ từ booking đã xác nhận hoặc hoàn thành
        Double totalRevenue = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.COMPLETED)
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        BigDecimal averageBookingValue = totalBookings > 0 && totalRevenue != null
                ? BigDecimal.valueOf(totalRevenue).divide(BigDecimal.valueOf(totalBookings), 0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Conversion rate = (confirmed + completed) / total * 100
        double conversionRate = totalBookings > 0
                ? Math.round(((confirmedCount + completedCount) * 100.0 / totalBookings) * 100.0) / 100.0
                : 0.0;

        BookingStats bookingStats = BookingStats.builder()
                .totalBookings(totalBookings)
                .pendingCount(pendingCount)
                .confirmedCount(confirmedCount)
                .completedCount(completedCount)
                .rejectedCount(rejectedCount)
                .cancelledCount(cancelledCount)
                .conversionRate(conversionRate)
                .build();

        return RevenueOverview.builder()
                .totalRevenue(BigDecimal.valueOf(totalRevenue).setScale(0, RoundingMode.HALF_UP))
                .totalBookings(totalBookings)
                .averageBookingValue(averageBookingValue)
                .bookingStats(bookingStats)
                .build();
    }

    private List<RevenueByDate> calculateRevenueByDate(List<Booking> bookings, LocalDate startDate, LocalDate endDate) {
        // Group bookings by date
        Map<LocalDate, List<Booking>> bookingsByDate = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.groupingBy(b ->
                    b.getBookingItems().get(0).getStartTime().toLocalDate()
                ));

        List<RevenueByDate> result = new ArrayList<>();

        // Generate all dates in range (kể cả ngày không có booking)
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            List<Booking> dayBookings = bookingsByDate.getOrDefault(currentDate, Collections.emptyList());
            Double revenue = dayBookings.stream()
                    .mapToDouble(Booking::getTotalPrice)
                    .sum();

            result.add(RevenueByDate.builder()
                    .date(currentDate)
                    .revenue(BigDecimal.valueOf(revenue).setScale(0, RoundingMode.HALF_UP))
                    .bookingCount((long) dayBookings.size())
                    .build());

            currentDate = currentDate.plusDays(1);
        }

        return result;
    }

    private List<RevenueByWeek> calculateRevenueByWeek(List<Booking> bookings, LocalDate startDate, LocalDate endDate) {
        // Tính 5 tuần trong khoảng thời gian
        List<RevenueByWeek> result = new ArrayList<>();

        LocalDate weekStart = startDate;
        int weekNumber = 1;

        while (weekStart.isBefore(endDate) || weekStart.isEqual(endDate)) {
            LocalDate weekEnd = weekStart.plusDays(6);
            if (weekEnd.isAfter(endDate)) {
                weekEnd = endDate;
            }

            LocalDate finalWeekStart = weekStart;
            LocalDate finalWeekEnd = weekEnd;

            List<Booking> weekBookings = bookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.COMPLETED)
                    .filter(b -> {
                        LocalDate bookingDate = b.getBookingItems().get(0).getStartTime().toLocalDate();
                        return !bookingDate.isBefore(finalWeekStart) && !bookingDate.isAfter(finalWeekEnd);
                    })
                    .collect(Collectors.toList());

            Double revenue = weekBookings.stream()
                    .mapToDouble(Booking::getTotalPrice)
                    .sum();

            result.add(RevenueByWeek.builder()
                    .weekNumber(weekNumber)
                    .weekLabel("Tuần " + weekNumber)
                    .weekStart(weekStart)
                    .weekEnd(weekEnd)
                    .weekRange(formatDateRange(weekStart, weekEnd))
                    .revenue(BigDecimal.valueOf(revenue).setScale(0, RoundingMode.HALF_UP))
                    .bookingCount((long) weekBookings.size())
                    .build());

            weekStart = weekStart.plusWeeks(1);
            weekNumber++;

            if (result.size() >= 5) break; // Giới hạn 5 tuần
        }

        return result;
    }

    private List<RevenueByMonth> calculateRevenueByMonth(List<Booking> bookings, LocalDate startDate, LocalDate endDate) {
        // Group by year-month
        Map<String, List<Booking>> bookingsByMonth = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.groupingBy(b -> {
                    LocalDate date = b.getBookingItems().get(0).getStartTime().toLocalDate();
                    return date.getYear() + "-" + date.getMonthValue();
                }));

        List<RevenueByMonth> result = new ArrayList<>();

        // Generate 12 months
        LocalDate currentMonth = endDate.minusMonths(11).withDayOfMonth(1);

        for (int i = 0; i < 12; i++) {
            int year = currentMonth.getYear();
            int month = currentMonth.getMonthValue();
            String key = year + "-" + month;

            List<Booking> monthBookings = bookingsByMonth.getOrDefault(key, Collections.emptyList());
            Double revenue = monthBookings.stream()
                    .mapToDouble(Booking::getTotalPrice)
                    .sum();

            result.add(RevenueByMonth.builder()
                    .year(year)
                    .month(month)
                    .monthName(currentMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .revenue(BigDecimal.valueOf(revenue).setScale(0, RoundingMode.HALF_UP))
                    .bookingCount((long) monthBookings.size())
                    .build());

            currentMonth = currentMonth.plusMonths(1);
        }

        return result;
    }

    private List<VenuePerformance> calculateVenuePerformance(List<Booking> bookings) {
        // Group by venue
        Map<Long, List<Booking>> bookingsByVenue = new HashMap<>();
        Map<Long, String> venueNames = new HashMap<>();

        for (Booking booking : bookings) {
            for (BookingItem item : booking.getBookingItems()) {
                Long venueId = item.getCourt().getVenues().getId();
                String venueName = item.getCourt().getVenues().getName();

                bookingsByVenue.computeIfAbsent(venueId, k -> new ArrayList<>()).add(booking);
                venueNames.put(venueId, venueName);
            }
        }

        return bookingsByVenue.entrySet().stream()
                .map(entry -> {
                    Long venueId = entry.getKey();
                    List<Booking> venueBookings = entry.getValue();

                    long completedBookings = venueBookings.stream()
                            .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                            .count();

                    Double revenue = venueBookings.stream()
                            .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.COMPLETED)
                            .mapToDouble(Booking::getTotalPrice)
                            .sum();

                    return VenuePerformance.builder()
                            .id(venueId)
                            .name(venueNames.get(venueId))
                            .bookingCount((long) venueBookings.size())
                            .revenue(BigDecimal.valueOf(revenue).setScale(0, RoundingMode.HALF_UP))
                            .completedBookings(completedBookings)
                            .build();
                })
                .sorted(Comparator.comparing(VenuePerformance::getRevenue).reversed())
                .collect(Collectors.toList());
    }

    private List<TopCustomer> calculateTopCustomers(List<Booking> bookings, int limit) {
        // Group by user
        Map<Long, List<Booking>> bookingsByUser = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.groupingBy(b -> b.getUser().getId()));

        return bookingsByUser.entrySet().stream()
                .map(entry -> {
                    Long userId = entry.getKey();
                    List<Booking> userBookings = entry.getValue();
                    String userName = userBookings.get(0).getUser().getFullname();
                    String userPhone = userBookings.get(0).getUser().getPhone();

                    Double totalSpent = userBookings.stream()
                            .mapToDouble(Booking::getTotalPrice)
                            .sum();

                    // Tìm ngày booking gần nhất
                    LocalDate lastBookingDate = userBookings.stream()
                            .flatMap(b -> b.getBookingItems().stream())
                            .map(item -> item.getStartTime().toLocalDate())
                            .max(LocalDate::compareTo)
                            .orElse(null);

                    return TopCustomer.builder()
                            .userId(userId)
                            .userName(userName)
                            .userPhone(userPhone)
                            .bookingCount((long) userBookings.size())
                            .totalSpent(BigDecimal.valueOf(totalSpent).setScale(0, RoundingMode.HALF_UP))
                            .lastBookingDate(lastBookingDate)
                            .build();
                })
                .sorted(Comparator.comparing(TopCustomer::getTotalSpent).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<TimeSlotStats> calculateTimeSlotStats(List<Booking> bookings) {
        // Group by hour
        Map<Integer, List<Booking>> bookingsByHour = new HashMap<>();

        for (Booking booking : bookings) {
            if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.COMPLETED) {
                continue;
            }
            for (BookingItem item : booking.getBookingItems()) {
                int hour = item.getStartTime().getHour();
                bookingsByHour.computeIfAbsent(hour, k -> new ArrayList<>()).add(booking);
            }
        }

        // Generate stats for all hours 0-23
        return IntStream.range(0, 24)
                .mapToObj(hour -> {
                    List<Booking> hourBookings = bookingsByHour.getOrDefault(hour, Collections.emptyList());
                    Double revenue = hourBookings.stream()
                            .mapToDouble(Booking::getTotalPrice)
                            .sum();

                    return TimeSlotStats.builder()
                            .hour(hour)
                            .hourLabel(String.format("%02d:00", hour))
                            .bookingCount((long) hourBookings.size())
                            .revenue(BigDecimal.valueOf(revenue).setScale(0, RoundingMode.HALF_UP))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<PaymentMethodStats> calculatePaymentMethodStats(List<Booking> bookings) {
        // Hiện tại chỉ có bank transfer
        List<Booking> paidBookings = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.toList());

        long totalCount = paidBookings.size();
        Double totalAmount = paidBookings.stream()
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        if (totalCount == 0) {
            return Collections.emptyList();
        }

        return Collections.singletonList(
                PaymentMethodStats.builder()
                        .method("BANK_TRANSFER")
                        .methodLabel("Chuyển khoản")
                        .count(totalCount)
                        .totalAmount(BigDecimal.valueOf(totalAmount).setScale(0, RoundingMode.HALF_UP))
                        .percentage(100.0)
                        .build()
        );
    }

    private Insights calculateInsights(List<TimeSlotStats> timeSlotStats,
                                       List<VenuePerformance> venuePerformance,
                                       List<RevenueByDate> revenueByDate) {
        // Peak hour
        TimeSlotStats peakHourStat = timeSlotStats.stream()
                .max(Comparator.comparing(TimeSlotStats::getBookingCount))
                .orElse(null);

        Integer peakHour = peakHourStat != null ? peakHourStat.getHour() : null;
        String peakHourLabel = peakHourStat != null ? peakHourStat.getHourLabel() : null;
        Long peakHourBookings = peakHourStat != null ? peakHourStat.getBookingCount() : 0L;

        // Best venue
        BestVenue bestVenue = null;
        if (!venuePerformance.isEmpty()) {
            VenuePerformance best = venuePerformance.get(0);
            bestVenue = BestVenue.builder()
                    .venueId(best.getId())
                    .venueName(best.getName())
                    .revenue(best.getRevenue())
                    .build();
        }

        // Best day
        BestDay bestDay = null;
        if (!revenueByDate.isEmpty()) {
            RevenueByDate best = revenueByDate.stream()
                    .max(Comparator.comparing(RevenueByDate::getRevenue))
                    .orElse(null);
            if (best != null) {
                bestDay = BestDay.builder()
                        .date(best.getDate())
                        .revenue(best.getRevenue())
                        .bookingCount(best.getBookingCount())
                        .build();
            }
        }

        // Growth rate - TODO: cần query kỳ trước để tính
        Double growthRate = 0.0;
        String growthRateLabel = "0%";

        return Insights.builder()
                .peakHour(peakHour)
                .peakHourLabel(peakHourLabel)
                .peakHourBookings(peakHourBookings)
                .bestVenue(bestVenue)
                .bestDay(bestDay)
                .growthRate(growthRate)
                .growthRateLabel(growthRateLabel)
                .build();
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        return String.format("%02d/%02d - %02d/%02d",
                start.getDayOfMonth(), start.getMonthValue(),
                end.getDayOfMonth(), end.getMonthValue());
    }
}

