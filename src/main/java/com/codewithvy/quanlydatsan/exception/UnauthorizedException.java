package com.codewithvy.quanlydatsan.exception;

/**
 * Ném khi người dùng không có quyền truy cập tài nguyên.
 * Dùng để trả về 403 Forbidden trong GlobalExceptionHandler.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

