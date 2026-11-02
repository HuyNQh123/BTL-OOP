package com.smartenvelope.repository;

import com.smartenvelope.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository thao tác dữ liệu với bảng Alert.
 * Phụ trách bởi: Thành viên 4.
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    // TODO: Thành viên 4 định nghĩa các query methods (findByUserIdAndIsReadFalse...)
}

