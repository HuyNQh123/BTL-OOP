package com.smartenvelope.controller.budget;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller xử lý các chức năng thiết lập kỳ ngân sách và phân bổ ngân sách.
 * Phụ trách bởi: Thành viên 4.
 */
@Controller
@RequestMapping("/budgets")
public class BudgetController {

    @GetMapping
    public String list() {
        return "budget/list";
    }

    @GetMapping("/new")
    public String form() {
        return "budget/form";
    }
}
