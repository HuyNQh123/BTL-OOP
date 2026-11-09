package com.smartenvelope.dto.transaction;

import com.smartenvelope.entity.Transaction;
import com.smartenvelope.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(Long id, String accountName, String envelopeName, String categoryName,
        BigDecimal amount, TransactionType type, LocalDate transactionDate, String note) {
    public TransactionResponse(Transaction t) {
        this(t.getId(), t.getAccount().getName(), t.getEnvelope() == null ? null : t.getEnvelope().getName(),
                t.getCategory().getName(), t.getAmount(), t.getType(), t.getTransactionDate(), t.getNote());
    }
}

