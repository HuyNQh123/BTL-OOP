package com.smartenvelope.dto.report;

import com.smartenvelope.enums.TransactionType;
import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDate;

/**
 * DTO chứa điều kiện lọc báo cáo thống kê (khoảng thời gian từ ngày - đến ngày, loại giao dịch, danh mục).
 * Phụ trách bởi: Thành viên 5.
 */
public record ReportFilterRequest(LocalDate startDate, LocalDate endDate,
        TransactionType transactionType, Long categoryId) {
    @AssertTrue(message = "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !startDate.isAfter(endDate);
    }
}

