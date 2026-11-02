package com.smartenvelope.controller.alert;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller xử lý các chức năng xem và quản lý thông báo, cảnh báo chi tiêu.
 * Phụ trách bởi: Thành viên 4.
 */
@Controller
@RequestMapping("/alerts")
public class AlertController {

    @GetMapping
    public String list() {
        return "alert/list";
    }
}
