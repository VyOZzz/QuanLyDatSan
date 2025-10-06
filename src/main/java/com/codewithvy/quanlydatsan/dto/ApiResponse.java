package com.codewithvy.quanlydatsan.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ApiResponse<T> {
    private boolean success;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
    private String message;
    private Instant timestamp;

    public static <T> ApiResponse<T> ok(T data, String message){
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
    public static <T> ApiResponse<T> ok(T data){
        return ok(data, "OK");
    }
    public static <T> ApiResponse<T> fail(String message){
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}

