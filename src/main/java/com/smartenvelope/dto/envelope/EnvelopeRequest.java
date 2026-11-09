package com.smartenvelope.dto.envelope;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record EnvelopeRequest(
        @NotNull(message = "Vui lòng chọn kỳ ngân sách.") Long budgetPeriodId,
        @NotNull(message = "Vui lòng chọn danh mục.") Long categoryId,
        @Size(max = 100, message = "Tên phong bì tối đa 100 ký tự.") String name,
        @NotNull(message = "Vui lòng nhập số tiền phân bổ.") @DecimalMin(value = "0", message = "Số tiền không được âm.") BigDecimal allocatedAmount) {
}

