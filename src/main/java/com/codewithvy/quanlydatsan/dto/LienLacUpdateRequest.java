package com.codewithvy.quanlydatsan.dto;

import com.codewithvy.quanlydatsan.entity.LienLacStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để cập nhật trạng thái và phản hồi liên lạc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LienLacUpdateRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private LienLacStatus status;

    // Ghi chú phản hồi (optional)
    private String response;
}
