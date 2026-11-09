package com.smartenvelope.dto.transaction;

import com.smartenvelope.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull(message = "Vui lòng chọn loại giao dịch.") TransactionType type,
        @NotNull(message = "Vui lòng nhập số tiền.") @DecimalMin(value = "1", message = "Số tiền phải lớn hơn 0.") BigDecimal amount,
        @NotNull(message = "Vui lòng chọn tài khoản.") Long accountId,
        Long envelopeId,
        Long sourceEnvelopeId,
        @NotNull(message = "Vui lòng chọn danh mục.") Long categoryId,
        @NotNull(message = "Vui lòng nhập ngày giao dịch.") LocalDate transactionDate,
        @Size(max = 500, message = "Ghi chú tối đa 500 ký tự.") String note) {
}

