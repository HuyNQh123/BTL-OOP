package com.smartenvelope.controller.savings;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller xử lý các chức năng quản lý mục tiêu tiết kiệm và heo đất điện tử.
 * Phụ trách bởi: Thành viên 4.
 */
@Controller
@RequestMapping("/savings")
public class SavingsGoalController {

    @GetMapping
    public String list() {
        return "savings/list";
    }

    @GetMapping("/new")
    public String form() {
        return "savings/form";
    }
}
