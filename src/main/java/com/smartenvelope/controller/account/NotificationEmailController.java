package com.smartenvelope.controller.account;

import com.smartenvelope.service.NotificationEmailVerificationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account/email")
public class NotificationEmailController {

    private final NotificationEmailVerificationService verificationService;

    public NotificationEmailController(NotificationEmailVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping
    public String show() {
        return "redirect:/alerts#notification-email";
    }

    @PostMapping("/send-code")
    public String sendCode(@RequestParam String notificationEmail, RedirectAttributes redirectAttributes) {
        try {
            verificationService.sendCode(notificationEmail);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi mã xác minh tới Gmail của bạn.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/alerts#notification-email";
    }

    @PostMapping("/verify")
    public String verify(@RequestParam String code, RedirectAttributes redirectAttributes) {
        if (verificationService.verifyCode(code)) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Gmail đã được xác minh. Thông báo mới sẽ được gửi tới địa chỉ này.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Mã xác minh không đúng, đã hết hạn hoặc vượt quá số lần nhập. Hãy yêu cầu mã mới.");
        }
        return "redirect:/alerts#notification-email";
    }
}
