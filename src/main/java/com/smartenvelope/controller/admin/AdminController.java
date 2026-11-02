package com.smartenvelope.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller xử lý các chức năng quản trị viên (danh sách người dùng, danh mục dùng chung).
 * Phụ trách bởi: Thành viên 1.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/users")
    public String users() {
        return "admin/users";
    }

    @GetMapping("/categories")
    public String categories() {
        return "admin/categories";
    }
}
