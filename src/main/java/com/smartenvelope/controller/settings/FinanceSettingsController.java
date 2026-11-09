package com.smartenvelope.controller.settings;

import com.smartenvelope.dto.settings.FinanceSettingsRequest;
import com.smartenvelope.service.FinanceSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/settings/finance")
public class FinanceSettingsController {

    private final FinanceSettingsService financeSettingsService;

    @GetMapping
    public String form(@RequestParam(defaultValue = "false") boolean edit, Model model) {
        model.addAttribute("settings", FinanceSettingsRequest.from(financeSettingsService.getCurrentSettings()));
        model.addAttribute("editMode", edit || !financeSettingsService.hasCurrentSettings());
        return "settings/finance";
    }

    @PostMapping
    public String update(@Valid FinanceSettingsRequest settings, BindingResult result, Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("settings", settings);
            model.addAttribute("editMode", true);
            return "settings/finance";
        }
        financeSettingsService.updateCurrentSettings(settings);
        redirectAttributes.addFlashAttribute("successMessage", "Cài đặt tài chính đã được lưu.");
        return "redirect:/settings/finance";
    }
}
