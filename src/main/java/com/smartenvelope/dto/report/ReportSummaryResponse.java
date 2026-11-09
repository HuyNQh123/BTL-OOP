package com.smartenvelope.dto.report;

import java.math.BigDecimal;
import java.util.Map;

public record ReportSummaryResponse(BigDecimal totalIncome, BigDecimal totalExpense,
        BigDecimal netSavings, Map<String, BigDecimal> categoryBreakdowns) { }

