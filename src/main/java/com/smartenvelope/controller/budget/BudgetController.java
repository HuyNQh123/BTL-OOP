package com.smartenvelope.controller.budget;

import com.smartenvelope.dto.budget.BudgetRequest;
import com.smartenvelope.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;
import java.time.ZoneId;

/**
 * Controller xử lý các chức năng thiết lập kỳ ngân sách và phân bổ ngân sách.
 * Phụ trách bởi: Thành viên 4.
 */
@Controller
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public String list(Model model) {

        model.addAttribute("budgets", budgetService.getCurrentWorkspaceBudgets());
        return "budget/list";
    }

    @GetMapping("/new")
    public String form(Model model) {

        YearMonth currentMonth = YearMonth.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        BudgetRequest request = new BudgetRequest(
                "Ngân sách tháng " + currentMonth.getMonthValue(),
                currentMonth.getMonthValue(), currentMonth.getYear(),
                budgetService.getExpectedSalary().signum() > 0 ? budgetService.getExpectedSalary() : null, null);

        model.addAttribute("budgetRequest", request);

        return "budget/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var budget = budgetService.getBudget(id);
        model.addAttribute("budgetId", id);
        model.addAttribute("budgetRequest", new BudgetRequest(budget.getName(), budget.getMonth(), budget.getYear(),
                budget.getTotalIncome(), budget.getRolloverPolicy()));
        return "budget/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute BudgetRequest budgetRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            return "budget/form";
        }

        try {

            budgetService.createBudget(budgetRequest);

            redirectAttributes.addFlashAttribute("successMessage", "Kỳ ngân sách đã được tạo.");

            return "redirect:/budgets";

        } catch (IllegalArgumentException ex) {

            model.addAttribute("errorMessage", ex.getMessage());
            return "budget/form";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute BudgetRequest budgetRequest,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("budgetId", id);
            return "budget/form";
        }
        try {
            budgetService.updateDraftBudget(id, budgetRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Kỳ ngân sách đã được cập nhật.");
            return "redirect:/budgets";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("budgetId", id);
            model.addAttribute("errorMessage", ex.getMessage());
            return "budget/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            budgetService.deleteDraftBudget(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa kỳ ngân sách nháp và các phong bì chưa phát sinh lịch sử.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/budgets";
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {

            budgetService.confirmBudget(id);

            redirectAttributes.addFlashAttribute("successMessage", "Kỳ ngân sách đã được xác nhận.");

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/budgets";
    }
}
