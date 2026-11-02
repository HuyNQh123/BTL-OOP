package com.smartenvelope.entity;

import jakarta.persistence.*;

/**
 * Entity đại diện cho kế hoạch ngân sách tổng thể và quy tắc quản lý tài chính.
 * Phụ trách bởi: Thành viên 4.
 */
@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Thành viên 4 khai báo các thuộc tính (user, name, totalBudget, rules, createdAt...)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
