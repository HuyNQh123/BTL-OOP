package com.smartenvelope.entity;

import jakarta.persistence.*;

/**
 * Entity đại diện cho chu kỳ ngân sách (theo tháng, tuần hoặc tùy chỉnh).
 * Phụ trách bởi: Thành viên 2.
 */
@Entity
@Table(name = "budget_periods")
public class BudgetPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Thành viên 2 khai báo các thuộc tính (user, name, startDate, endDate, totalIncome...)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
