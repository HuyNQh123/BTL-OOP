package com.smartenvelope.controller.transaction;

import com.smartenvelope.dto.transaction.TransactionRequest;
import com.smartenvelope.enums.TransactionType;
import com.smartenvelope.service.TransactionService;
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
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Controller xử lý các chức năng ghi nhận giao dịch thu, chi và chuyển tiền.
 * Phụ trách bởi: Thành viên 3.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("transactions", transactionService.getCurrentWorkspaceTransactions());
        return "transaction/list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        TransactionType defaultType = transactionService.hasConfirmedBudgetFor(today)
                ? TransactionType.EXPENSE : TransactionType.INCOME;
        prepareForm(model, new TransactionRequest(defaultType, null, null, null, null, null, today, ""));
        return "transaction/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute TransactionRequest transactionRequest, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepareForm(model, transactionRequest);
            return "transaction/form";
        }
        try {
            transactionService.createTransaction(transactionRequest);
            if (transactionRequest.type() == TransactionType.INCOME) {
                redirect.addFlashAttribute("successMessage",
                        "Đã ghi nhận thu nhập. Hãy phân bổ khoản tiền mới tại trang ngân sách.");
                return "redirect:/budgets";
            }
            if (transactionRequest.type() == TransactionType.EXPENSE) {
                BigDecimal hours = transactionService.effortHoursForCurrentUser(transactionRequest.amount());
                if (hours.signum() > 0) {
                    String amount = java.text.NumberFormat.getIntegerInstance(java.util.Locale.forLanguageTag("vi-VN"))
                            .format(transactionRequest.amount());
                    redirect.addFlashAttribute("successMessage", "Đã ghi nhận khoản chi " + amount
                            + " ₫. Khoản chi này tương đương khoảng "
                            + hours.stripTrailingZeros().toPlainString() + " giờ làm theo mức thu nhập/giờ bạn đã cài đặt."
                            + (transactionRequest.envelopeId() == null && transactionRequest.sourceEnvelopeId() == null
                                    ? " Khoản chi được ghi nhận ngoài phong bì vì danh mục chưa có phong bì trong kỳ."
                                    : ""));
                    return "redirect:/transactions";
                }
            }
            redirect.addFlashAttribute("successMessage", transactionRequest.type() == TransactionType.EXPENSE
                    && transactionRequest.envelopeId() == null && transactionRequest.sourceEnvelopeId() == null
                    ? "Giao dịch đã được ghi nhận ngoài phong bì vì danh mục chưa có phong bì trong kỳ."
                    : "Giao dịch đã được ghi nhận.");
            return "redirect:/transactions";
        } catch (IllegalArgumentException ex) {
            prepareForm(model, transactionRequest);
            model.addAttribute("errorMessage", ex.getMessage());
            return "transaction/form";
        }
    }

    private void prepareForm(Model model, TransactionRequest request) {
        LocalDate transactionDate = request.transactionDate() != null ? request.transactionDate()
                : LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        model.addAttribute("transactionRequest", request);
        model.addAttribute("accounts", transactionService.getCurrentWorkspaceAccounts());
        model.addAttribute("envelopes", transactionService.getCurrentWorkspaceEnvelopes());
        model.addAttribute("categories", transactionService.getAvailableCategories());
        model.addAttribute("transactionTypes", TransactionType.values());
        model.addAttribute("hourlyWage", transactionService.getHourlyWageForCurrentUser());
        model.addAttribute("hasConfirmedBudget", transactionService.hasConfirmedBudgetFor(transactionDate));
    }
}

