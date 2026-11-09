package com.smartenvelope.controller.user;

import com.smartenvelope.dto.user.ProfileUpdateRequest;
import com.smartenvelope.service.CurrentUserService;
import com.smartenvelope.service.ProfileEmailChangeService;
import com.smartenvelope.service.ProfileService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final CurrentUserService currentUserService;
    private final ProfileService profileService;
    private final ProfileEmailChangeService profileEmailChangeService;

    @GetMapping
    public String show(Model model, HttpSession session) {
        model.addAttribute("profile", ProfileUpdateRequest.from(currentUserService.getCurrentUser()));
        model.addAttribute("currentEmail", currentUserService.getCurrentUser().getEmail());
        model.addAttribute("pendingEmail", profileEmailChangeService.getPendingEmail(session));
        return "profile/index";
    }

    @PostMapping
    public String update(@Valid ProfileUpdateRequest profile, BindingResult result, Model model,
            RedirectAttributes redirectAttributes, HttpSession session) {
        if (result.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("currentEmail", currentUserService.getCurrentUser().getEmail());
            model.addAttribute("pendingEmail", profileEmailChangeService.getPendingEmail(session));
            return "profile/index";
        }
        try {
            profileService.updateProfile(profile);
            redirectAttributes.addFlashAttribute("successMessage", "Thông tin cá nhân đã được cập nhật.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/email/send-code")
    public String sendEmailCode(@RequestParam String newEmail, HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            profileEmailChangeService.sendCode(newEmail, session);
            redirectAttributes.addFlashAttribute("successMessage", "Mã xác minh đã được gửi tới email mới.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/email/verify")
    public String verifyEmailCode(@RequestParam String code, HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            if (profileEmailChangeService.verifyAndUpdateEmail(code, session)) {
                redirectAttributes.addFlashAttribute("successMessage", "Email tài khoản đã được đổi và xác minh.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Mã xác minh không đúng, đã hết hạn hoặc vượt quá số lần nhập. Hãy yêu cầu mã mới.");
            }
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/profile";
    }
}
