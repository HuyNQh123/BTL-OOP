package com.smartenvelope.dto.budget;

import com.smartenvelope.entity.BudgetPeriod;
import com.smartenvelope.enums.BudgetStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record BudgetResponse(
        Long id,
        String name,
        Integer month,
        Integer year,
        BigDecimal totalIncome,
        BigDecimal totalAllocated,
        BigDecimal remainingBudget,
        BudgetStatus status,
        LocalDate startDate,
        LocalDate endDate) {

    public BudgetResponse(BudgetPeriod period) {
        this(period.getId(), period.getName(), period.getMonth(), period.getYear(),
                period.getTotalIncome(), period.getTotalAllocated(),
                period.getTotalIncome().subtract(period.getTotalAllocated()), period.getStatus(),
                period.getStartDate(), period.getEndDate());
    }

    public boolean canConfirm() {
        return status == BudgetStatus.DRAFT && totalIncome.compareTo(totalAllocated) == 0;
    }

    public int allocationPercent() {
        if (totalIncome == null || totalIncome.signum() <= 0 || totalAllocated == null) return 0;
        return totalAllocated.max(BigDecimal.ZERO).min(totalIncome)
                .multiply(BigDecimal.valueOf(100)).divide(totalIncome, 0, RoundingMode.DOWN).intValue();
    }
}
