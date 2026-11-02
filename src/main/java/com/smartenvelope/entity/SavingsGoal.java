package com.smartenvelope.entity;

import jakarta.persistence.*;

/**
 * Entity đại diện cho mục tiêu tiết kiệm tài chính cá nhân.
 * Phụ trách bởi: Thành viên 4.
 */
@Entity
@Table(name = "savings_goals")
public class SavingsGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Thành viên 4 khai báo các thuộc tính (user, name, targetAmount, currentAmount, deadline, createdAt...)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
