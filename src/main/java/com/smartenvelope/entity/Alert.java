package com.smartenvelope.entity;

import jakarta.persistence.*;

/**
 * Entity đại diện cho cảnh báo hoặc thông báo chi tiêu gửi tới người dùng.
 * Phụ trách bởi: Thành viên 4.
 */
@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Thành viên 4 khai báo các thuộc tính (user, type, message, isRead, createdAt...)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
