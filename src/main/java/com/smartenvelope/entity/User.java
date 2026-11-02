package com.smartenvelope.entity;

import jakarta.persistence.*;

/**
 * Entity đại diện cho bảng User trong cơ sở dữ liệu.
 * Phụ trách bởi: Thành viên 1.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Thành viên 1 khai báo các thuộc tính (fullName, email, password, role, enabled, locked, createdAt...)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

