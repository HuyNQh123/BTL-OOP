package com.smartenvelope.service;

import com.smartenvelope.entity.Transaction;
import com.smartenvelope.enums.BudgetStatus;
import com.smartenvelope.enums.TransactionType;
import com.smartenvelope.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

/** Counts consecutive days with expenses recorded against a confirmed budget envelope. */
@Service
public class BudgetStreakService {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final TransactionRepository transactionRepository;

    public BudgetStreakService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public int getCurrentStreak(Long userId, Long workspaceId) {
        if (userId == null || workspaceId == null) {
            return 0;
        }

        LocalDate today = LocalDate.now(APP_ZONE);
        Set<LocalDate> allocatedExpenseDays = new HashSet<>();
        for (Transaction transaction : transactionRepository
                .findByUserIdAndWorkspaceIdAndTransactionDateBetween(userId, workspaceId,
                        today.minusDays(365), today)) {
            if (transaction.getType() != TransactionType.EXPENSE || transaction.getEnvelope() == null
                    || transaction.getEnvelope().getBudgetPeriod() == null) {
                continue;
            }
            BudgetStatus status = transaction.getEnvelope().getBudgetPeriod().getStatus();
            if (status == BudgetStatus.CONFIRMED || status == BudgetStatus.CLOSED) {
                allocatedExpenseDays.add(transaction.getTransactionDate());
            }
        }

        LocalDate streakEnd = allocatedExpenseDays.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (streak < 365 && allocatedExpenseDays.contains(streakEnd.minusDays(streak))) {
            streak++;
        }
        return streak;
    }
}
