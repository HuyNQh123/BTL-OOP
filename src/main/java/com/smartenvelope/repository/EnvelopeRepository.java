package com.smartenvelope.repository;

import com.smartenvelope.entity.Envelope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository thao tác dữ liệu với bảng Envelope.
 * Phụ trách bởi: Thành viên 2.
 */
@Repository
public interface EnvelopeRepository extends JpaRepository<Envelope, Long> {
    // TODO: Thành viên 2 định nghĩa các query methods (findByBudgetPeriodId...)
}

