package com.codewithvy.quanlydatsan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenuesDTO {
    private Long id;
    private String name;
    private int numberOfCourt;
    private AddressDTO address;
    private Integer courtsCount; // tránh load toàn bộ courts
}

