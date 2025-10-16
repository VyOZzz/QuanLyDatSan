// file: src/main/java/com/codewithvy/quanlydatsan/service/PriceService.java
package com.codewithvy.quanlydatsan.service;

import com.codewithvy.quanlydatsan.entity.PriceRules;
import com.codewithvy.quanlydatsan.repository.PriceRuleRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
public class PriceService {

    private final PriceRuleRepository priceRuleRepository;
    private static final int SLOT_DURATION_MINUTES = 30;

    public PriceService(PriceRuleRepository priceRuleRepository) {
        this.priceRuleRepository = priceRuleRepository;
    }

    /**
     * Tính tổng chi phí cho một khoảng thời gian đặt sân, dựa trên các khối 30 phút.
     *
     * @param venueId ID của khu sân (venue)
     * @param startDateTime Thời gian bắt đầu đặt
     * @param endDateTime Thời gian kết thúc đặt
     * @return Tổng chi phí, hoặc Optional.empty() nếu có lỗi (ví dụ: thiếu quy tắc giá).
     */
    public Optional<Double> calculateTotalCost(Long venueId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // --- Validation ---
        if (startDateTime.isAfter(endDateTime) || startDateTime.isEqual(endDateTime)) {
            return Optional.of(0.0); // Thời gian không hợp lệ
        }
        // Kiểm tra xem khoảng thời gian có phải là bội số của 30 phút không
        long minutes = Duration.between(startDateTime, endDateTime).toMinutes();
        if (minutes % SLOT_DURATION_MINUTES != 0) {
            // Có thể trả về lỗi hoặc làm tròn, ở đây ta trả về lỗi để đảm bảo tính đúng đắn
            // throw new IllegalArgumentException("Booking duration must be a multiple of 30 minutes.");
            return Optional.empty(); // Hoặc trả về empty để controller xử lý
        }

        // --- Calculation ---
        double totalCost = 0.0;
        LocalDateTime currentSlotStart = startDateTime;

        // Lặp qua từng khối 30 phút
        while (currentSlotStart.isBefore(endDateTime)) {
            LocalTime time = currentSlotStart.toLocalTime();

            // Tìm quy tắc giá áp dụng cho thời điểm bắt đầu của khối này
            Optional<PriceRules> rule = priceRuleRepository.findApplicablePriceRule(venueId, time);

            if (rule.isEmpty()) {
                // Nếu không có quy tắc giá cho một khối nào đó, không thể tính tổng chi phí
                return Optional.empty();
            }

            // Giá mỗi giờ theo quy tắc
            long pricePerHour = rule.get().getPricePerHour();
            // Giá cho một khối 30 phút là giá mỗi giờ chia 2
            double costForSlot = pricePerHour / 2.0;

            totalCost += costForSlot;

            // Di chuyển đến khối 30 phút tiếp theo
            currentSlotStart = currentSlotStart.plusMinutes(SLOT_DURATION_MINUTES);
        }

        return Optional.of(totalCost);
    }
}
