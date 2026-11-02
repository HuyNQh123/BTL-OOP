package com.smartenvelope.entity;

import jakarta.persistence.*;

/**
 * Entity đại diện cho tài khoản nguồn tiền (Tiền mặt, Ngân hàng, Ví điện tử) của người dùng.
 * Phụ trách bởi: Thành viên 2.
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Thành viên 2 khai báo các thuộc tính (user, name, type, balance, createdAt...)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

