package com.smartenvelope.controller.report;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller xử lý báo cáo thống kê, biểu đồ thu chi và phân tích tài chính.
 * Phụ trách bởi: Thành viên 5.
 */
@Controller
@RequestMapping("/reports")
public class ReportController {

    @GetMapping
    public String index() {
        return "report/index";
    }
}
