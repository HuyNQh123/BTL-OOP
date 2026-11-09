package com.smartenvelope.controller.report;

import com.smartenvelope.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import java.time.YearMonth;
import java.time.ZoneId;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller xử lý báo cáo thống kê, biểu đồ thu chi và phân tích tài chính.
 * Phụ trách bởi: Thành viên 5.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reportService;

    @GetMapping
    public String index(@RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year, Model model) {
        YearMonth period = (month == null || year == null) ? YearMonth.now(ZoneId.of("Asia/Ho_Chi_Minh")) : YearMonth.of(year, month);
        model.addAttribute("month", period.getMonthValue());
        model.addAttribute("year", period.getYear());
        var summary = reportService.getSummary(period.atDay(1), period.atEndOfMonth());
        model.addAttribute("summary", summary);
        Map<String, Integer> categoryPercentages = new LinkedHashMap<>();
        summary.categoryBreakdowns().forEach((category, amount) -> {
            int percentage = summary.totalExpense().signum() == 0 ? 0
                    : amount.multiply(BigDecimal.valueOf(100))
                            .divide(summary.totalExpense(), 0, java.math.RoundingMode.DOWN).intValue();
            categoryPercentages.put(category, percentage);
        });
        model.addAttribute("categoryPercentages", categoryPercentages);
        model.addAttribute("rescueMap", reportService.getBudgetRescueMap(period.getMonthValue(), period.getYear()));
        model.addAttribute("disciplineBadge", reportService.getDisciplineBadge(period.getMonthValue(), period.getYear()));
        return "report/index";
    }
}

