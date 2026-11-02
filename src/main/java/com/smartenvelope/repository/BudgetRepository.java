package com.smartenvelope.repository;

import com.smartenvelope.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository thao tác dữ liệu với bảng Budget.
 * Phụ trách bởi: Thành viên 4.
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    // TODO: Thành viên 4 định nghĩa các query methods (findByUserId...)
}
