package com.smartenvelope.controller.dashboard;

import com.smartenvelope.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller xử lý hiển thị bảng điều khiển (Dashboard) tổng quan tài chính cá nhân.
 * Phụ trách bởi: Thành viên 5.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public String index(Model model) {
        var summary = dashboardService.getSummary();
        model.addAttribute("summary", summary);
        Map<Long, Integer> envelopePercentages = new LinkedHashMap<>();
        Map<Long, Integer> envelopeWidths = new LinkedHashMap<>();
        summary.envelopes().forEach(envelope -> {
            int percentage = envelope.getAllocatedAmount().signum() == 0 ? 0
                    : envelope.getSpentAmount().multiply(BigDecimal.valueOf(100))
                            .divide(envelope.getAllocatedAmount(), 0, java.math.RoundingMode.DOWN).intValue();
            envelopePercentages.put(envelope.getId(), percentage);
            envelopeWidths.put(envelope.getId(), Math.min(100, percentage));
        });
        model.addAttribute("envelopePercentages", envelopePercentages);
        model.addAttribute("envelopeWidths", envelopeWidths);
        return "dashboard/index";
    }
}

