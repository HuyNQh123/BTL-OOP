package com.smartenvelope.service;

import com.smartenvelope.entity.BudgetPeriod;
import com.smartenvelope.entity.Envelope;
import com.smartenvelope.entity.Transaction;
import com.smartenvelope.enums.BudgetStatus;
import com.smartenvelope.enums.TransactionStatus;
import com.smartenvelope.enums.TransactionType;
import com.smartenvelope.repository.AccountRepository;
import com.smartenvelope.repository.BudgetPeriodRepository;
import com.smartenvelope.repository.EnvelopeRepository;
import com.smartenvelope.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {
    private final WorkspaceContextService workspaceContext;
    private final AccountRepository accountRepository;
    private final BudgetPeriodRepository budgetRepository;
    private final EnvelopeRepository envelopeRepository;
    private final TransactionRepository transactionRepository;

    public Summary getSummary() {
        Long workspaceId = workspaceContext.getCurrentWorkspaceId();
        BigDecimal balance = accountRepository.findByWorkspaceId(workspaceId).stream()
                .map(a -> a.getBalance()).reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        BudgetPeriod budget = budgetRepository.findByWorkspaceIdAndMonthAndYear(workspaceId,
                today.getMonthValue(), today.getYear()).orElse(null);
        List<Envelope> envelopes = budget == null ? List.of() : envelopeRepository.findByBudgetPeriodId(budget.getId());
        List<Transaction> transactions = transactionRepository.findByWorkspaceIdAndTransactionDateBetween(
                workspaceId, today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth())).stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED).toList();
        BigDecimal spent = sum(transactions, TransactionType.EXPENSE);
        BigDecimal income = sum(transactions, TransactionType.INCOME);
        BigDecimal allocated = envelopes.stream().map(Envelope::getAllocatedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = envelopes.stream().map(Envelope::getRemainingAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Summary(balance, income, spent, allocated, remaining, budget == null ? null : budget.getStatus(), envelopes);
    }

    private BigDecimal sum(List<Transaction> transactions, TransactionType type) {
        return transactions.stream().filter(t -> t.getType() == type).map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public record Summary(BigDecimal accountBalance, BigDecimal monthlyIncome, BigDecimal monthlyExpense,
                          BigDecimal allocated, BigDecimal envelopeBalance, BudgetStatus budgetStatus,
                          List<Envelope> envelopes) { }
}

