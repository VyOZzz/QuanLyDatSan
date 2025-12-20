package com.codewithvy.quanlydatsan.dto;

import com.codewithvy.quanlydatsan.entity.LienLacStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO để trả về thông tin liên lạc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LienLacDTO {

    private Long id;

    private Long userId;

    private String userFullname;

    private String name;

    private String email;

    private String phone;

    private String subject;

    private String message;

    private LienLacStatus status;

    private Long venueId;

    private String venueName;

    private Long handlerId;

    private String handlerName;

    private String response;

    private Instant createdAt;

    private Instant updatedAt;
}
