package com.smartenvelope.dto.ai;

import com.smartenvelope.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AIParseResponse(BigDecimal suggestedAmount, String suggestedCategory,
        TransactionType suggestedType, String note, BigDecimal confidence, LocalDate transactionDate, String message) { }

