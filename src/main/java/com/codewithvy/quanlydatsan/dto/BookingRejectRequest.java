package com.codewithvy.quanlydatsan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request để từ chối booking
 */
@Data
public class BookingRejectRequest {
    @NotBlank(message = "Rejection reason is required")
    private String rejectionReason;
}

