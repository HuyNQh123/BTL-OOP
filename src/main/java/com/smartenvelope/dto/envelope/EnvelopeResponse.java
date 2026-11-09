package com.smartenvelope.dto.envelope;

import com.smartenvelope.entity.Envelope;
import com.smartenvelope.enums.BudgetStatus;
import java.math.BigDecimal;

public record EnvelopeResponse(Long id, Long budgetPeriodId, String budgetName, BudgetStatus budgetStatus, String name, String categoryName,
        BigDecimal allocatedAmount, BigDecimal spentAmount, BigDecimal remainingAmount, BigDecimal percentSpent,
        Boolean fixedEnvelope, BigDecimal dailyAllowance, Integer daysRemaining) {
    public EnvelopeResponse(Envelope e) {
        this(e.getId(), e.getBudgetPeriod().getId(), e.getBudgetPeriod().getName(), e.getBudgetPeriod().getStatus(), e.getName(), e.getCategory().getName(),
                e.getAllocatedAmount(), e.getSpentAmount(), e.getRemainingAmount(),
                e.getAllocatedAmount().signum() == 0 ? BigDecimal.ZERO : e.getSpentAmount().multiply(BigDecimal.valueOf(100))
                        .divide(e.getAllocatedAmount(), 2, java.math.RoundingMode.HALF_UP),
                Boolean.TRUE.equals(e.getFixedEnvelope()),
                e.getRemainingAmount().divide(BigDecimal.valueOf(Math.max(1,
                        java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")), e.getBudgetPeriod().getEndDate()) + 1)), 0, java.math.RoundingMode.DOWN),
                (int) Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")), e.getBudgetPeriod().getEndDate()) + 1));
    }
}

