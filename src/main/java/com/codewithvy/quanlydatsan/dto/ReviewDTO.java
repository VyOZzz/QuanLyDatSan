package com.codewithvy.quanlydatsan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO để trả về thông tin review.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {

    private Long id;

    private Long userId;

    private String userFullname;

    private Long venueId;

    private String venueName;

    private Long bookingId;

    private Integer rating;

    private String comment;

    private Instant createdAt;

    private Instant updatedAt;
}
