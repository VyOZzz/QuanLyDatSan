package com.codewithvy.quanlydatsan.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPlainText(String to, String subject, String body){
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception ex){
            // Với môi trường dev nếu chưa cấu hình SMTP sẽ lỗi. Ghi log để không làm hỏng luồng chính.
            log.warn("Cannot send email to {} - cause: {}", to, ex.getMessage());
        }
    }
}

