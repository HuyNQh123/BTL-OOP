package com.smartenvelope.repository;

import com.smartenvelope.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository thao tác dữ liệu với bảng SavingsGoal.
 * Phụ trách bởi: Thành viên 4.
 */
@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {
    // TODO: Thành viên 4 định nghĩa các query methods (findByUserId...)
}

