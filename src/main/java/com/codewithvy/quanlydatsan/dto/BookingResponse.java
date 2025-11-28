package com.codewithvy.quanlydatsan.dto;

import com.codewithvy.quanlydatsan.entity.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookingResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userPhoneNumber; // Số điện thoại người đặt sân
    private Long courtId;
    private String courtName;
    private String venuesName;
    private AddressDTO venueAddress; // Địa chỉ sân
    private String venuePhoneNumber; // Số điện thoại venue
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double totalPrice;
    private BookingStatus status;
    private LocalDateTime expireTime;
    private Boolean paymentProofUploaded;
    private String paymentProofUrl;
    private LocalDateTime paymentProofUploadedAt;
    private String rejectionReason;
    private OwnerBankInfoDTO ownerBankInfo; // Thông tin TK chủ sân
    private List<BookingItemResponse> bookingItems; // ✅ Danh sách chi tiết các sân đã đặt
}
