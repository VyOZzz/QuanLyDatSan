// file: src/main/java/com/codewithvy/quanlydatsan/service/PriceService.java
package com.codewithvy.quanlydatsan.service;

import com.codewithvy.quanlydatsan.entity.Venues;
import com.codewithvy.quanlydatsan.repository.VenuesRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PriceService {

    private final VenuesRepository venuesRepository;
    private static final int SLOT_DURATION_MINUTES = 30;

    public PriceService(VenuesRepository venuesRepository) {
        this.venuesRepository = venuesRepository;
    }

    /**
     * Tính tổng chi phí cho một khoảng thời gian đặt sân, dựa trên giá cố định của venue.
     *
     * @param venueId ID của khu sân (venue)
     * @param startDateTime Thời gian bắt đầu đặt
     * @param endDateTime Thời gian kết thúc đặt
     * @return Tổng chi phí, hoặc Optional.empty() nếu có lỗi.
     */
    public Optional<Double> calculateTotalCost(Long venueId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // --- Validation ---
        if (startDateTime.isAfter(endDateTime) || startDateTime.isEqual(endDateTime)) {
            return Optional.of(0.0); // Thời gian không hợp lệ
        }

        // Kiểm tra xem khoảng thời gian có phải là bội số của 30 phút không
        long minutes = Duration.between(startDateTime, endDateTime).toMinutes();
        if (minutes % SLOT_DURATION_MINUTES != 0) {
            return Optional.empty(); // Thời gian phải là bội số của 30 phút
        }

        // Lấy thông tin venue
        Optional<Venues> venueOpt = venuesRepository.findById(venueId);
        if (venueOpt.isEmpty()) {
            return Optional.empty(); // Venue không tồn tại
        }

        Venues venue = venueOpt.get();
        Double pricePerHour = venue.getPricePerHour();

        // Tính số giờ
        double hours = minutes / 60.0;

        // Tổng chi phí = số giờ * giá mỗi giờ
        double totalCost = hours * pricePerHour;

        return Optional.of(totalCost);
    }
}
