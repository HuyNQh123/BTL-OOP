package com.smartenvelope.controller.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller xử lý hiển thị bảng điều khiển (Dashboard) tổng quan tài chính cá nhân.
 * Phụ trách bởi: Thành viên 5.
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @GetMapping
    public String index() {
        return "dashboard/index";
    }
}

