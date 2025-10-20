package com.codewithvy.quanlydatsan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request để upload chứng minh chuyển khoản
 */
@Data
public class PaymentProofRequest {
    @NotBlank(message = "Payment proof URL is required")
    private String paymentProofUrl;
}

