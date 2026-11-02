package com.smartenvelope.entity;

import jakarta.persistence.*;

/**
 * Entity đại diện cho giao dịch tài chính (Thu, Chi, Chuyển khoản).
 * Phụ trách bởi: Thành viên 3.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Thành viên 3 khai báo các thuộc tính (user, account, envelope, category, amount, type, transactionDate, note...)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

