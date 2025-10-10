package com.codewithvy.quanlydatsan.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller demo đơn giản để kiểm tra app chạy thành công.
 */
@RestController // Đánh dấu đây là một Controller chuyên trả về dữ liệu (API)
public class HelloController {

    /**
     * GET /hello: trả về chuỗi chào xác nhận ứng dụng đã chạy.
     */
    @GetMapping("/hello") // Lắng nghe request tại địa chỉ /hello
    public String sayHello() {
        return "Project đã chạy thành công!";
    }
}