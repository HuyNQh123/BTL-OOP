package com.smartenvelope.controller.transaction;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller xử lý các chức năng ghi nhận giao dịch thu, chi và chuyển tiền.
 * Phụ trách bởi: Thành viên 3.
 */
@Controller
@RequestMapping("/transactions")
public class TransactionController {

    @GetMapping
    public String list() {
        return "transaction/list";
    }

    @GetMapping("/new")
    public String form() {
        return "transaction/form";
    }
}

