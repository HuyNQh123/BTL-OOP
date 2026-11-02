package com.smartenvelope.entity;

import jakarta.persistence.*;

/**
 * Entity đại diện cho heo đất điện tử (tích lũy lẻ và tiền gửi định kỳ).
 * Phụ trách bởi: Thành viên 4.
 */
@Entity
@Table(name = "piggy_banks")
public class PiggyBank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Thành viên 4 khai báo các thuộc tính (savingsGoal, accumulatedAmount, lastDepositDate...)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

