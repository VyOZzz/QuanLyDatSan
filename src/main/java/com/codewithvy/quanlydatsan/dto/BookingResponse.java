package com.codewithvy.quanlydatsan.dto;

import com.codewithvy.quanlydatsan.entity.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long courtId;
    private String courtName;
    private String venuesName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double totalPrice;
    private Long pricePerHour;  // Giá mỗi giờ của venue
    private BookingStatus status;
    private LocalDateTime expireTime;
    private Boolean paymentProofUploaded;
    private String paymentProofUrl;
    private LocalDateTime paymentProofUploadedAt;
    private String rejectionReason;
    private OwnerBankInfoDTO ownerBankInfo; // Thông tin TK chủ sân
}
