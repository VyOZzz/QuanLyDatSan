package com.codewithvy.quanlydatsan.service;

import com.codewithvy.quanlydatsan.dto.ReviewDTO;
import com.codewithvy.quanlydatsan.dto.ReviewRequest;
import com.codewithvy.quanlydatsan.entity.*;
import com.codewithvy.quanlydatsan.exception.ResourceNotFoundException;
import com.codewithvy.quanlydatsan.repository.BookingRepository;
import com.codewithvy.quanlydatsan.repository.BookingItemRepository;
import com.codewithvy.quanlydatsan.repository.ReviewRepository;
import com.codewithvy.quanlydatsan.repository.UserRepository;
import com.codewithvy.quanlydatsan.repository.VenuesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final UserRepository userRepository;
    private final VenuesRepository venuesRepository;
    private final NotificationService notificationService;

    /**
     * Tạo review mới cho một booking đã hoàn thành.
     * Chỉ cho phép USER tạo review cho booking của chính họ và booking phải ở trạng thái COMPLETED.
     */
    @Transactional
    public ReviewDTO createReview(Long bookingId, ReviewRequest request, String userPhone) {
        // 1. Kiểm tra user
        User user = userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Kiểm tra booking tồn tại
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        // 3. Kiểm tra booking thuộc về user này
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only review your own bookings");
        }

        // 4. Kiểm tra booking đã được xác nhận hoặc hoàn thành chưa
        // Cho phép review nếu booking đã CONFIRMED (đã được chấp nhận) hoặc COMPLETED (đã hoàn thành)
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("You can only review confirmed or completed bookings");
        }

        // 5. Kiểm tra đã review chưa
        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new IllegalArgumentException("You have already reviewed this booking");
        }

        // 6. Lấy thông tin venue từ BookingItem
        List<BookingItem> items = bookingItemRepository.findByBookingId(bookingId);
        if (items.isEmpty()) {
            throw new ResourceNotFoundException("No booking items found for this booking");
        }
        Venues venue = items.get(0).getCourt().getVenues();

        // 7. Tạo review mới
        Review review = new Review();
        review.setUser(user);
        review.setVenues(venue);
        review.setBooking(booking);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);

        // 8. XÓA - Không cần cập nhật venue rating nữa, vì sẽ tính toán động

        // 9. Gửi thông báo cho chủ sân
        String title = "Đánh giá mới";
        String message = String.format("%s đã đánh giá %d sao cho %s",
                user.getFullname(), request.getRating(), venue.getName());
        notificationService.createNotification(
                venue.getOwner(),    // recipient
                user,                 // sender
                booking,             // booking
                NotificationType.REVIEW_RECEIVED,
                title,
                message
        );

        return mapToDTO(savedReview);
    }

    /**
     * Lấy tất cả review của một venue.
     */
    public List<ReviewDTO> getReviewsByVenue(Long venueId) {
        // Kiểm tra venue tồn tại
        if (!venuesRepository.existsById(venueId)) {
            throw new ResourceNotFoundException("Venue not found with id: " + venueId);
        }

        List<Review> reviews = reviewRepository.findByVenuesIdOrderByCreatedAtDesc(venueId);
        return reviews.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả review của một user.
     */
    public List<ReviewDTO> getReviewsByUser(String userPhone) {
        User user = userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return reviews.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy review của một booking cụ thể.
     */
    public ReviewDTO getReviewByBooking(Long bookingId, String userPhone) {
        User user = userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Review review = reviewRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found for this booking"));

        // Chỉ cho phép user xem review của booking của họ
        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only view your own reviews");
        }

        return mapToDTO(review);
    }

    /**
     * Xóa review (chỉ cho phép user xóa review của chính họ).
     */
    @Transactional
    public void deleteReview(Long reviewId, String userPhone) {
        User user = userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        // Chỉ cho phép user xóa review của chính họ
        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);

        // XÓA - Không cần cập nhật lại rating, vì tính toán động
    }

    /**
     * Map Review entity sang ReviewDTO.
     */
    private ReviewDTO mapToDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userFullname(review.getUser().getFullname())
                .venueId(review.getVenues().getId())
                .venueName(review.getVenues().getName())
                .bookingId(review.getBooking().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
