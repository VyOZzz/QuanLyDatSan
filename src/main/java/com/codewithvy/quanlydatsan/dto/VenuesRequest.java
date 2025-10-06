package com.codewithvy.quanlydatsan.dto;

import lombok.Data;

@Data
public class VenuesRequest {
    private String name;
    private Integer numberOfCourt;
    private Long addressId; // chỉ cần id, backend sẽ load Address
}

