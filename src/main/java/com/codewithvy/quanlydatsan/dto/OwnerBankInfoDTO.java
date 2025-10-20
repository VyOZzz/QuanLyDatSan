package com.codewithvy.quanlydatsan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa thông tin tài khoản ngân hàng của chủ sân
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerBankInfoDTO {
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
    private String ownerName;
}
