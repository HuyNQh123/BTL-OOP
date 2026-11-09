package com.smartenvelope.dto.settings;

import com.smartenvelope.entity.UserFinanceSettings;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalTime;

public record FinanceSettingsRequest(
        @NotNull(message = "Vui lòng nhập ngày nhận lương.")
        @Min(value = 1, message = "Ngày nhận lương phải từ 1 đến 31.")
        @Max(value = 31, message = "Ngày nhận lương phải từ 1 đến 31.")
        Integer salaryDay,

        @NotNull(message = "Vui lòng nhập mức lương dự kiến.")
        @DecimalMin(value = "0", message = "Mức lương không được âm.")
        BigDecimal expectedSalary,

        @NotNull(message = "Vui lòng nhập thu nhập theo giờ (có thể nhập 0 nếu không dùng).")
        @DecimalMin(value = "0", message = "Thu nhập theo giờ không được âm.")
        BigDecimal hourlyWage,

        @NotNull(message = "Vui lòng chọn giờ nhắc phân bổ ngân sách.")
        @DateTimeFormat(pattern = "HH:mm")
        LocalTime budgetReminderTime,

        @NotNull(message = "Vui lòng chọn giờ nhắc dừng chi tiêu.")
        @DateTimeFormat(pattern = "HH:mm")
        LocalTime dailyReminderTime) {

    public static FinanceSettingsRequest from(UserFinanceSettings settings) {
        return new FinanceSettingsRequest(settings.getSalaryDay(), settings.getExpectedSalary(), settings.getHourlyWage(),
                settings.getBudgetReminderTime(), settings.getDailyReminderTime());
    }
}
