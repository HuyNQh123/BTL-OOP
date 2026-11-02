package com.smartenvelope.repository;

import com.smartenvelope.entity.BudgetPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository thao tác dữ liệu với bảng BudgetPeriod.
 * Phụ trách bởi: Thành viên 2.
 */
@Repository
public interface BudgetPeriodRepository extends JpaRepository<BudgetPeriod, Long> {
    // TODO: Thành viên 2 định nghĩa các query methods (findByUserId...)
}

