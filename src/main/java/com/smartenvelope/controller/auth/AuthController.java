package com.smartenvelope.controller.auth;

import com.smartenvelope.dto.auth.RegisterRequest;
import com.smartenvelope.service.AuthService;
import com.smartenvelope.service.RegistrationEmailVerificationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller điều hướng và xử lý xác thực (Đăng nhập, Đăng ký).
 * Phụ trách bởi: Thành viên 1.
 */
@Controller
public class AuthController {

    private final AuthService authService;
    private final RegistrationEmailVerificationService emailVerificationService;

    public AuthController(AuthService authService, RegistrationEmailVerificationService emailVerificationService) {
        this.authService = authService;
        this.emailVerificationService = emailVerificationService;
    }

    /**
     * Trang chủ hệ thống: Nếu đã đăng nhập thì vào Dashboard, nếu chưa thì vào trang Login.
     */
    @GetMapping("/")
    public String index() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    /**
     * Hiển thị trang Đăng nhập.
     */
    @GetMapping({"/login", "/auth/login"})
    public String login() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    /**
     * Hiển thị trang Đăng ký tài khoản mới.
     */
    @GetMapping({"/register", "/auth/register"})
    public String showRegisterForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "auth/register";
    }

    @PostMapping("/register/send-code")
    public ResponseEntity<String> sendRegistrationEmailCode(@RequestParam String email, HttpSession session) {
        if (authService.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email này đã được sử dụng.");
        }
        try {
            emailVerificationService.sendCode(email, session);
            return ResponseEntity.ok("Đã gửi mã xác minh. Mã có hiệu lực trong 10 phút.");
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(exception.getMessage());
        }
    }

    /**
     * Tiếp nhận và xử lý dữ liệu form Đăng ký tài khoản.
     */
    @PostMapping({"/register", "/auth/register"})
    public String handleRegister(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {

        // 1. Kiểm tra xác nhận mật khẩu
        if (request.getPassword() != null && request.getConfirmPassword() != null
                && !request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Mật khẩu xác nhận không khớp");
        }

        // 2. Kiểm tra trùng email
        if (request.getEmail() != null && authService.existsByEmail(request.getEmail())) {
            bindingResult.rejectValue("email", "error.email", "Địa chỉ email này đã được sử dụng");
        }

        // 3. Kiểm tra trùng username (nếu có nhập)
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()
                && authService.existsByUsername(request.getUsername())) {
            bindingResult.rejectValue("username", "error.username", "Tên đăng nhập này đã được sử dụng");
        }

        // Nếu có lỗi validation, quay lại form đăng ký kèm thông báo lỗi chi tiết
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        if (!emailVerificationService.verify(request.getEmail(), request.getEmailVerificationCode(), session)) {
            bindingResult.rejectValue("emailVerificationCode", "error.emailVerificationCode",
                    "Mã xác minh không đúng, đã hết hạn hoặc chưa được gửi tới email này.");
            return "auth/register";
        }

        try {
            authService.register(request);
            emailVerificationService.clear(session);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Đăng ký tài khoản thành công! Vui lòng đăng nhập để bắt đầu quản lý tài chính.");
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/register";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Đã xảy ra lỗi trong quá trình khởi tạo tài khoản. Vui lòng thử lại sau.");
            return "auth/register";
        }
    }
}
