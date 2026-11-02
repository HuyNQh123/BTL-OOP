package com.smartenvelope.repository;

import com.smartenvelope.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository thao tác dữ liệu với bảng Transaction.
 * Phụ trách bởi: Thành viên 3.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // TODO: Thành viên 3 định nghĩa các query methods (findByUserId, findByAccountId, findByEnvelopeId...)
}
