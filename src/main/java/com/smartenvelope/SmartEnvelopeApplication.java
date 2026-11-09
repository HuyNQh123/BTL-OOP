package com.smartenvelope;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Lớp khởi chạy ứng dụng SmartEnvelope.
 * Website quản lý ngân sách và chi tiêu thông minh.
 * Phụ trách bởi: Toàn nhóm (Dùng chung).
 */
@SpringBootApplication
@EnableScheduling
public class SmartEnvelopeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartEnvelopeApplication.class, args);
    }
}

