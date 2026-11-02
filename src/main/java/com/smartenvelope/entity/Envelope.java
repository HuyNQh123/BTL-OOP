package com.smartenvelope.entity;

import jakarta.persistence.*;

/**
 * Entity đại diện cho phong bì ngân sách (chứa hạn mức chi tiêu cho từng danh mục theo kỳ).
 * Phụ trách bởi: Thành viên 2.
 */
@Entity
@Table(name = "envelopes")
public class Envelope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Thành viên 2 khai báo các thuộc tính (budgetPeriod, category, name, allocatedAmount, spentAmount...)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

