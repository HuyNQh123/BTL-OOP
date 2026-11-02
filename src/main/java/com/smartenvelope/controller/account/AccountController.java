package com.smartenvelope.controller.account;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller xử lý các chức năng quản lý tài khoản nguồn tiền (tiền mặt, ngân hàng, ví điện tử).
 * Phụ trách bởi: Thành viên 2.
 */
@Controller
@RequestMapping("/accounts")
public class AccountController {

    @GetMapping
    public String list() {
        return "account/list";
    }

    @GetMapping("/new")
    public String form() {
        return "account/form";
    }
}
