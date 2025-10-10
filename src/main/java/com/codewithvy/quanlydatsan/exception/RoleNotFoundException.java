package com.codewithvy.quanlydatsan.exception;

/**
 * Ném khi không tìm thấy role theo tên yêu cầu (ví dụ khi gán role lúc đăng ký).
 * Dùng để trả về lỗi 400 trong GlobalExceptionHandler.
 */
public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String message){super(message);}
}
