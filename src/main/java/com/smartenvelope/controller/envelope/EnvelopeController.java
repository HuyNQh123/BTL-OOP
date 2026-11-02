package com.smartenvelope.controller.envelope;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller xử lý các chức năng quản lý phong bì ngân sách.
 * Phụ trách bởi: Thành viên 2.
 */
@Controller
@RequestMapping("/envelopes")
public class EnvelopeController {

    @GetMapping
    public String list() {
        return "envelope/list";
    }

    @GetMapping("/new")
    public String form() {
        return "envelope/form";
    }
}

