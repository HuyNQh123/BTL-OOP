package com.smartenvelope.controller.envelope;

import com.smartenvelope.dto.envelope.EnvelopeRequest;
import com.smartenvelope.service.EnvelopeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý các chức năng quản lý phong bì ngân sách.
 * Phụ trách bởi: Thành viên 2.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/envelopes")
public class EnvelopeController {
    private final EnvelopeService envelopeService;

    @GetMapping
    public String list(@RequestParam(required = false) Long budgetId, Model model) {
        model.addAttribute("envelopes", envelopeService.getCurrentWorkspaceEnvelopes(budgetId));
        model.addAttribute("selectedBudgetId", budgetId);
        if (budgetId != null) model.addAttribute("selectedBudget", envelopeService.getBudget(budgetId));
        return "envelope/list";
    }

    @GetMapping("/new")
    public String form(@RequestParam(required = false) Long budgetId, Model model) {
        if (budgetId != null) envelopeService.getEditableBudget(budgetId);
        prepareForm(model, new EnvelopeRequest(budgetId, null, "", null));
        return "envelope/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var envelope = envelopeService.getEditableEnvelope(id);
        prepareForm(model, new EnvelopeRequest(envelope.getBudgetPeriod().getId(),
                envelope.getCategory().getId(), envelope.getName(), envelope.getAllocatedAmount()));
        model.addAttribute("editing", true);
        model.addAttribute("envelopeId", id);
        model.addAttribute("formAction", "/envelopes/" + id);
        model.addAttribute("editingCategoryName", envelope.getCategory().getName());
        model.addAttribute("editingSpentAmount", envelope.getSpentAmount());
        return "envelope/form";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            Long budgetId = envelopeService.deleteDraftEnvelope(id);
            redirect.addFlashAttribute("successMessage", "Đã xóa phong bì nháp.");
            return "redirect:/envelopes?budgetId=" + budgetId;
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/envelopes";
        }
    }

    @PostMapping("/{id}/fixed")
    public String setFixed(@PathVariable Long id, @RequestParam boolean fixed, @RequestParam(required = false) Long budgetId, RedirectAttributes redirect) {
        try {
            envelopeService.setFixed(id, fixed);
            redirect.addFlashAttribute("successMessage", fixed ? "Đã khóa phong bì thiết yếu." : "Đã mở khóa phong bì.");
        } catch (IllegalArgumentException ex) { redirect.addFlashAttribute("errorMessage", ex.getMessage()); }
        return "redirect:/envelopes" + (budgetId == null ? "" : "?budgetId=" + budgetId);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute EnvelopeRequest envelopeRequest, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepareForm(model, envelopeRequest);
            return "envelope/form";
        }
        try {
            envelopeService.createEnvelope(envelopeRequest);
            redirect.addFlashAttribute("successMessage", "Phong bì đã được thêm vào kỳ ngân sách.");
            return "redirect:/envelopes";
        } catch (IllegalArgumentException ex) {
            prepareForm(model, envelopeRequest);
            model.addAttribute("errorMessage", ex.getMessage());
            return "envelope/form";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute EnvelopeRequest envelopeRequest,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepareForm(model, envelopeRequest);
            model.addAttribute("editing", true);
            model.addAttribute("envelopeId", id);
            model.addAttribute("formAction", "/envelopes/" + id);
            var envelope = envelopeService.getEditableEnvelope(id);
            model.addAttribute("editingCategoryName", envelope.getCategory().getName());
            model.addAttribute("editingSpentAmount", envelope.getSpentAmount());
            return "envelope/form";
        }
        try {
            Long budgetId = envelopeService.updateDraftEnvelope(id, envelopeRequest);
            redirect.addFlashAttribute("successMessage", "Đã cập nhật tên phong bì và hạn mức phân bổ.");
            return "redirect:/envelopes?budgetId=" + budgetId;
        } catch (IllegalArgumentException ex) {
            prepareForm(model, envelopeRequest);
            model.addAttribute("editing", true);
            model.addAttribute("envelopeId", id);
            model.addAttribute("formAction", "/envelopes/" + id);
            var envelope = envelopeService.getEditableEnvelope(id);
            model.addAttribute("editingCategoryName", envelope.getCategory().getName());
            model.addAttribute("editingSpentAmount", envelope.getSpentAmount());
            model.addAttribute("errorMessage", ex.getMessage());
            return "envelope/form";
        }
    }

    private void prepareForm(Model model, EnvelopeRequest request) {
        model.addAttribute("envelopeRequest", request);
        model.addAttribute("formAction", "/envelopes");
        model.addAttribute("budgets", envelopeService.getEditableBudgets());
        model.addAttribute("categories", envelopeService.getExpenseCategories());
    }
}

