package com.codewithvy.quanlydatsan.exception;

/**
 * Ném khi không tìm thấy tài nguyên (entity) yêu cầu.
 * Dùng để trả về 404 Not Found trong GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message){super(message);}
}
