package com.smartenvelope.controller.account;

import com.smartenvelope.dto.account.AccountRequest;
import com.smartenvelope.enums.AccountType;
import com.smartenvelope.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

/**
 * Controller xử lý các chức năng quản lý tài khoản nguồn tiền (tiền mặt, ngân hàng, ví điện tử).
 * Phụ trách bởi: Thành viên 2.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("accounts", accountService.getCurrentWorkspaceAccounts());
        return "account/list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("accountRequest", new AccountRequest("", AccountType.CASH, BigDecimal.ZERO));
        model.addAttribute("accountTypes", AccountType.values());
        return "account/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute AccountRequest accountRequest, BindingResult result, Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("accountTypes", AccountType.values());
            return "account/form";
        }
        try {
            accountService.createAccount(accountRequest);
            redirect.addFlashAttribute("successMessage", "Tài khoản đã được tạo.");
            return "redirect:/accounts";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("accountTypes", AccountType.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "account/form";
        }
    }
}

