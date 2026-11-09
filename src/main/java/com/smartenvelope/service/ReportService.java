package com.smartenvelope.service;

import com.smartenvelope.dto.report.ReportSummaryResponse;
import com.smartenvelope.entity.Transaction;
import com.smartenvelope.enums.TransactionStatus;
import com.smartenvelope.enums.TransactionType;
import com.smartenvelope.repository.TransactionRepository;
import com.smartenvelope.repository.BudgetPeriodRepository;
import com.smartenvelope.repository.EnvelopeTransferRepository;
import com.smartenvelope.entity.BudgetPeriod;
import com.smartenvelope.entity.EnvelopeTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {
    private final WorkspaceContextService workspaceContext;
    private final TransactionRepository transactionRepository;
    private final BudgetPeriodRepository budgetRepository;
    private final EnvelopeTransferRepository transferRepository;

    public ReportSummaryResponse getSummary(LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionRepository.findByWorkspaceIdAndTransactionDateBetween(
                workspaceContext.getCurrentWorkspaceId(), startDate, endDate).stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED).toList();
        BigDecimal income = total(transactions, TransactionType.INCOME);
        BigDecimal expense = total(transactions, TransactionType.EXPENSE);
        Map<String, BigDecimal> categories = transactions.stream().filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(t -> t.getCategory().getName(), LinkedHashMap::new,
                        Collectors.mapping(Transaction::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
        return new ReportSummaryResponse(income, expense, income.subtract(expense), categories);
    }

    private BigDecimal total(List<Transaction> transactions, TransactionType type) {
        return transactions.stream().filter(t -> t.getType() == type).map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, BigDecimal> getBudgetRescueMap(int month, int year) {
        BudgetPeriod budget = budgetRepository.findByWorkspaceIdAndMonthAndYear(
                workspaceContext.getCurrentWorkspaceId(), month, year).orElse(null);
        if (budget == null) return Map.of();
        return transferRepository.findByBudgetPeriodId(budget.getId()).stream()
                .filter(t -> t.getReason() == com.smartenvelope.enums.TransferReason.COVER_EXPENSE)
                .collect(Collectors.groupingBy(t -> t.getFromEnvelope().getName(), LinkedHashMap::new,
                        Collectors.mapping(EnvelopeTransfer::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
    }

    public String getDisciplineBadge(int month, int year) {
        BudgetPeriod budget = budgetRepository.findByWorkspaceIdAndMonthAndYear(
                workspaceContext.getCurrentWorkspaceId(), month, year).orElse(null);
        if (budget == null) return "Chưa có kỳ ngân sách để chấm điểm";
        BigDecimal moved = transferRepository.findByBudgetPeriodId(budget.getId()).stream()
                .filter(t -> t.getReason() == com.smartenvelope.enums.TransferReason.COVER_EXPENSE)
                .map(EnvelopeTransfer::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (moved.signum() == 0) return "Chiến thần kỷ luật";
        if (budget.getTotalIncome().signum() > 0 && moved.multiply(BigDecimal.valueOf(10))
                .compareTo(budget.getTotalIncome()) <= 0) return "Biết điều chỉnh kế hoạch";
        return "Chuyên gia lạm lẹm";
    }
}

