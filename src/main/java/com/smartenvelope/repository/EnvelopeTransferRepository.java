package com.smartenvelope.repository;

import com.smartenvelope.entity.EnvelopeTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository thao tác dữ liệu với bảng envelope_transfers. Phụ trách bởi: Thành
 * viên 3 (Transaction & Ledger Processing).
 */
public interface EnvelopeTransferRepository extends JpaRepository<EnvelopeTransfer, Long> {

    List<EnvelopeTransfer> findByBudgetPeriodId(Long budgetPeriodId);

    List<EnvelopeTransfer> findByFromEnvelopeId(Long fromEnvelopeId);

    List<EnvelopeTransfer> findByToEnvelopeId(Long toEnvelopeId);
}
