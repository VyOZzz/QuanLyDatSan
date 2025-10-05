package com.codewithvy.quanlydatsan.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // Đánh dấu đây là một Controller chuyên trả về dữ liệu (API)
public class HelloController {

    @GetMapping("/hello") // Lắng nghe request tại địa chỉ /hello
    public String sayHello() {
        return "Project đã chạy thành công!";
    }
}